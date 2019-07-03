package org.sinoc.contrdata.storage.dictionary;

import org.springframework.stereotype.*;
import java.util.stream.*;
import org.sinoc.util.*;
import org.sinoc.vm.DataWord;
import org.sinoc.vm.OpCode;
import org.sinoc.vm.VMHook;
import org.sinoc.vm.program.Program;
import org.sinoc.db.*;
import org.slf4j.*;
import java.util.*;
import java.util.function.*;

@Component
public class StorageDictionaryVmHook implements VMHook {
	private static Logger log;
	private StorageDictionaryDb dictionaryDb;
	private List<Layout.DictPathResolver> pathResolvers;
	private Stack<StorageKeys> storageKeysStack;
	private Stack<Sha3Index> sha3IndexStack;

	public StorageDictionaryVmHook(StorageDictionaryDb dictionaryDb, List<Layout.DictPathResolver> pathResolvers) {
		this.storageKeysStack = new Stack<StorageKeys>();
		this.sha3IndexStack = new Stack<Sha3Index>();
		this.dictionaryDb = dictionaryDb;
		this.pathResolvers = pathResolvers;
	}

	private byte[] getContractAddress(Program program) {
		return program.getOwnerAddress().getLast20Bytes();
	}

	public void startPlay(Program program) {
		try {
			this.storageKeysStack.push(new StorageKeys());
			this.sha3IndexStack.push(new Sha3Index());
		} catch (Throwable e) {
			StorageDictionaryVmHook.log.error("Error within handler: ", e);
		}
	}

	public void step(Program program, OpCode opcode) {
		try {
			org.sinoc.vm.program.Stack stack = program.getStack();
			switch (opcode) {
			case SSTORE: {
				DataWord key = (DataWord) stack.get(stack.size() - 1);
				DataWord value = (DataWord) stack.get(stack.size() - 2);
				this.storageKeysStack.peek().add(key, value);
				break;
			}
			case SHA3: {
				DataWord offset = (DataWord) stack.get(stack.size() - 1);
				DataWord size = (DataWord) stack.get(stack.size() - 2);
				byte[] input = program.memoryChunk(offset.intValue(), size.intValue());
				this.sha3IndexStack.peek().add(input);
				break;
			}
			default:
				break;
			}
		} catch (Throwable e) {
			StorageDictionaryVmHook.log.error("Error within handler: ", e);
		}
	}

	public void stopPlay(Program program) {
		try {
			byte[] address = this.getContractAddress(program);
			StorageKeys storageKeys = this.storageKeysStack.pop();
			Sha3Index sha3Index = this.sha3IndexStack.pop();
			Map<Layout.Lang, StorageDictionary> dictByLang = this.pathResolvers.stream().collect(Collectors.toMap(
					Layout.DictPathResolver::getLang, r -> this.dictionaryDb.getDictionaryFor(r.getLang(), address)));
			Map<Layout.Lang, StorageDictionary> map = new HashMap<>();
			storageKeys.forEach((key, removed) -> this.pathResolvers.forEach(resolver -> {
				StorageDictionary.PathElement[] path = resolver.resolvePath(key.getData(), sha3Index);
				StorageDictionary dictionary = map.get(resolver.getLang());
				dictionary.addPath(path);
			}));
			dictByLang.values().forEach(StorageDictionary::store);
			if (this.storageKeysStack.isEmpty()) {
				this.dictionaryDb.flush();
			}
		} catch (Throwable e) {
			StorageDictionaryVmHook.log.error(
					"Error within handler address[" + ByteUtil.toHexString(this.getContractAddress(program)) + "]: ",
					e);
		}
	}

	static {
		log = LoggerFactory.getLogger(StorageDictionaryVmHook.class);
	}

	private static class StorageKeys {
		private static DataWord REMOVED_VALUE;
		private Map<ByteArrayWrapper, Boolean> keys;

		private StorageKeys() {
			this.keys = new HashMap<ByteArrayWrapper, Boolean>();
		}

		public void add(DataWord key, DataWord value) {
			this.keys.put(new ByteArrayWrapper(key.getData()), this.isRemoved(value));
		}

		public void forEach(BiConsumer<? super ByteArrayWrapper, ? super Boolean> action) {
			this.keys.forEach(action);
		}

		private Boolean isRemoved(DataWord value) {
			return StorageKeys.REMOVED_VALUE.equals((Object) value);
		}

		static {
			REMOVED_VALUE = DataWord.ZERO;
		}
	}
}