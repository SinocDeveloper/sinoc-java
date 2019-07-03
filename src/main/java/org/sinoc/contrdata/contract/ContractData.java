package org.sinoc.contrdata.contract;

import java.util.regex.*;
import org.sinoc.contrdata.storage.dictionary.*;
import org.sinoc.contrdata.storage.*;
import org.apache.commons.lang3.math.*;
import org.sinoc.vm.*;
import org.apache.commons.lang3.*;
import java.util.stream.*;
import java.util.*;
import org.sinoc.util.*;
import java.util.function.*;
import org.spongycastle.util.encoders.*;

public class ContractData {
	private static Pattern DATA_WORD_PATTERN;
	private Ast.Contract contract;
	private Map<String, Members> structFields;
	private Members contractMembers;
	private Map<String, List<String>> enumValues;
	private StorageDictionary dictionary;
	private Map<Element, StorageDictionary.PathElement> elementTranslateMap;

	public ContractData(Ast.Contract contract, StorageDictionary dictionary) {
		this.elementTranslateMap = new HashMap<Element, StorageDictionary.PathElement>();
		this.dictionary = dictionary;
		this.contract = contract;
		this.contractMembers = Members.ofContract(this);
		this.structFields = contract.getStructures().stream()
				.collect(Collectors.toMap(Ast.Entry::getName,
						struct -> Members.ofStructure(this, struct)));
		this.enumValues = contract.getEnums().stream().collect(Collectors.toMap(Ast.Entry::getName,
				anEnum -> anEnum.getValues().stream().map(Ast.EnumValue::getName).collect(Collectors.toList())));
	}

	public Members getStructFields(Ast.Type.Struct struct) {
		return this.getStructFields(struct.getType());
	}

	public Members getStructFields(String name) {
		return this.structFields.get(name);
	}

	public Members getMembers() {
		return this.contractMembers;
	}

	public Ast.Contract getContract() {
		return this.contract;
	}

	public List<String> getEnumValues(Ast.Type.Enum enumType) {
		return this.enumValues.get(enumType.getType());
	}

	public String getEnumValueByOrdinal(Ast.Type.Enum enumType, int ordinal) {
		return this.getEnumValues(enumType).get(ordinal);
	}

	public static ContractData parse(String asJson, StorageDictionary dictionary) {
		Ast.Contract contract = Ast.Contract.fromJson(asJson);
		return new ContractData(contract, dictionary);
	}

	private StorageDictionary.PathElement translateToPathElement(Element element) {
		return this.elementTranslateMap.computeIfAbsent(element, el -> {
			String[] parts = el.dictionaryPath().parts();
			StorageDictionary.PathElement result = this.dictionary.getByPath(parts);
			return result;
		});
	}

	public Element elementByPath(Object... pathParts) {
		RootElementImpl rootElement = new RootElementImpl();
		Path path = Path.of(pathParts);
		if (path.isEmpty()) {
			return rootElement;
		}
		Iterator<String> itr = path.iterator();
		Member member = this.getMembers().get(NumberUtils.toInt((String) itr.next()));
		ElementImpl result = new ElementImpl(member, rootElement);
		while (itr.hasNext()) {
			result = new ElementImpl(itr.next(), result);
		}
		return result;
	}

	private static boolean isDataWord(String input) {
		return ContractData.DATA_WORD_PATTERN.matcher(input).matches();
	}

	private static DataWord extractPackedArrEl(DataWord slot, int index, int size) {
		byte[] data = slot.getData();
		int offset = (index + 1) % (32 / size) * size;
		int from = data.length - offset;
		int to = from + size;
		return new DataWord(ArrayUtils.subarray(data, from, to));
	}

	public StorageDictionary getDictionary() {
		return this.dictionary;
	}

	static {
		DATA_WORD_PATTERN = Pattern.compile("[0-9a-fA-f]{64}");
	}

	public abstract class Element {
		public Path path() {
			return Path.empty();
		}

		protected Path dictionaryPath() {
			return Path.empty();
		}

		public StorageDictionary.PathElement toDictionaryPathElement() {
			return ContractData.this.translateToPathElement(this);
		}

		public abstract int getChildrenCount(boolean p0);

		public int getChildrenCount() {
			return this.getChildrenCount(false);
		}

		public abstract List<Element> getChildren(int p0, int p1, boolean p2);

		public List<Element> getChildren(int page, int size) {
			return this.getChildren(page, size, false);
		}

		public List<Element> getAllChildren() {
			return this.getChildren(0, this.getChildrenCount());
		}

		public Ast.Type getType() {
			throw new UnsupportedOperationException();
		}

		public Element getParent() {
			return null;
		}

		public String getKey() {
			throw new UnsupportedOperationException();
		}

		public String getValue(Function<DataWord, DataWord> valueExtractor) {
			throw new UnsupportedOperationException();
		}

		public DataWord getStorageValue(Function<DataWord, DataWord> valueExtractor) {
			throw new UnsupportedOperationException();
		}

		public boolean isRoot() {
			return false;
		}

		Member getMember() {
			throw new UnsupportedOperationException();
		}
	}

	public class RootElementImpl extends Element {
		@Override
		public boolean isRoot() {
			return true;
		}

		@Override
		public int getChildrenCount(boolean ignoreEmpty) {
			return (ignoreEmpty ? this.getExistedMembers() : ContractData.this.getMembers()).size();
		}

		@Override
		public List<Element> getChildren(int page, int size, boolean ignoreEmpty) {
			Members members = ignoreEmpty ? this.getExistedMembers() : ContractData.this.getMembers();
			return members.page(page, size).stream().map(member -> new ElementImpl(member, this))
					.collect(Collectors.toList());
		}

		private Members getExistedMembers() {
			Set<Integer> storageIndexes = this.toDictionaryPathElement().getChildrenStream()
					.map(pe -> NumberUtils.toInt(pe.key))
					.collect(Collectors.toSet());
			return ContractData.this.getMembers().filter(m -> storageIndexes.contains(m.getStorageIndex()));
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof RootElementImpl)) {
				return false;
			}
			RootElementImpl other = (RootElementImpl) o;
			return other.canEqual(this);
		}

		protected boolean canEqual(Object other) {
			return other instanceof RootElementImpl;
		}

		@Override
		public int hashCode() {
			int result = 1;
			return result;
		}
	}

	public class ElementImpl extends Element {
		private String id;
		private Ast.Type type;
		private Element parent;
		private Member member;

		private ElementImpl(String id, Ast.Type type, Element previous) {
			this.id = id;
			this.type = type;
			this.parent = previous;
		}

		ElementImpl(Member member, Element previous) {
			this(String.valueOf(member.getPosition()), member.getType(), previous);
			this.member = member;
		}

		ElementImpl(String id, ElementImpl previous) {
			if (previous.getType().isStruct()) {
				this.member = getStructFields(previous.getType().asStruct())
						.findByPosition(NumberUtils.toInt(id));
			}
		}

		@SuppressWarnings("unused")
		private Ast.Type nestedType(String id) {
			if (this.type.isMapping()) {
				return this.type.asMapping().getValueType();
			}
			if (this.type.isArray()) {
				return this.type.asArray().getElementType();
			}
			if (this.type.isStruct()) {
				return ContractData.this.getStructFields(this.type.asStruct()).findByPosition(NumberUtils.toInt(id))
						.getType();
			}
			throw new UnsupportedOperationException("Elementary type hasn't nested types");
		}

		@Override
		public Path path() {
			return this.getParent().path().extend(this.id);
		}

		public Path dictionaryPath() {
			Path path = this.getParent().dictionaryPath();
			if (this.getParent().isRoot()) {
				return path.extend(this.member.getStorageIndex());
			}
			Ast.Type parentType = this.getParent().getType();
			if (parentType.isStaticArray()) {
				float reservedSlotsCount = this.type.isStruct()
						? ContractData.this.getStructFields(this.type.asStruct()).reservedSlotsCount()
						: (Member.size(this.type) / 32.0f);
				int startIndex = NumberUtils.toInt(path.removeLast())
						+ (int) (NumberUtils.toInt(this.id) * reservedSlotsCount);
				return path.extend(startIndex);
			}
			if (parentType.isStructArray()) {
				int fieldsCount = ContractData.this.getStructFields(this.type.asStruct()).reservedSlotsCount();
				return path.extend(NumberUtils.toInt(this.id) * fieldsCount);
			}
			Element grandParent = this.getParent().getParent();
			if (parentType.isStruct() && (grandParent.isRoot() || !grandParent.getType().isMapping())) {
				int startIndex = this.member.getStorageIndex() + NumberUtils.toInt(path.removeLast());
				return path.extend(startIndex);
			}
			if (parentType.isStruct() && grandParent.getType().isMapping()) {
				return path.extend(this.member.getStorageIndex());
			}
			return path.extend(this.id);
		}

		private List<Integer> arrIndexes() {
			if (!this.type.isArray()) {
				throw new UnsupportedOperationException("Can't get indexes for non array element.");
			}
			Set<Integer> indexes = new HashSet<Integer>();
			int slotsPerElement = 1;
			if (this.type.isStructArray()) {
				Ast.Type.Struct structType = this.type.asArray().getElementType().asStruct();
				slotsPerElement = ContractData.this.getStructFields(structType).reservedSlotsCount();
			}
			if (this.type.isStaticArray()) {
				int offset = this.member.getStorageIndex();
				if (32 / Member.size(this.type.asArray().getElementType()) > 1) {
					IntStream.range(0, this.type.asArray().getSize()).forEach(indexes::add);
				} else {
					int size = this.type.asArray().getSize() * slotsPerElement;
					for (StorageDictionary.PathElement child : this.getParent().toDictionaryPathElement()
							.getChildren()) {
						int current = NumberUtils.toInt(child.key) - offset;
						if (current >= size) {
							break;
						}
						if (current < 0) {
							continue;
						}
						indexes.add(current / slotsPerElement);
					}
				}
			} else {
				StorageDictionary.PathElement element = this.toDictionaryPathElement();
				if (element != null) {
					for (StorageDictionary.PathElement child2 : element.getChildren()) {
						int current2 = NumberUtils.toInt(child2.key);
						indexes.add(current2 / slotsPerElement);
					}
				}
			}
			return indexes.stream().sorted().collect(Collectors.toList());
		}

		@Override
		public int getChildrenCount(boolean ignoreEmpty) {
			int result = 0;
			if (this.type.isContainer()) {
				StorageDictionary.PathElement element = this.toDictionaryPathElement();
				if (element != null) {
					result = element.getChildrenCount();
				}
				if (this.type.isArray()) {
					result = this.arrIndexes().size();
				}
			} else if (this.type.isStruct()) {
				result = ContractData.this.getStructFields(this.type.asStruct()).size();
			}
			return result;
		}

		@Override
		public List<Element> getChildren(int page, int size, boolean ignoreEmpty) {
			List<Element> result = Collections.emptyList();
			int offset = page * size;
			int fromIndex = Math.max(0, offset);
			int toIndex = Math.min(this.getChildrenCount(ignoreEmpty), offset + size);
			if (fromIndex < toIndex) {
				if (this.type.isStruct()) {
					result = ContractData.this.getStructFields(this.type.asStruct()).page(page, size).stream()
							.map(field -> new ElementImpl(field, this))
							.collect(Collectors.toList());
				} else if (this.type.isArray()) {
					result = this.arrIndexes().subList(fromIndex, toIndex).stream()
							.map(i -> new ElementImpl(String.valueOf(i), this.type.asArray().getElementType(), this))
							.collect(Collectors.toList());
				} else if (this.type.isMapping()) {
					result = this.toDictionaryPathElement().getChildren(page * size, size).stream()
							.map(pe -> new ElementImpl(pe.key, this))
							.collect(Collectors.toList());
				}
			}
			return result;
		}

		@Override
		public String getKey() {
			String result = this.id;
			if (this.member != null) {
				result = this.member.getName();
			} else if (this.getParent().getType().isMapping() && isDataWord(this.id)) {
				Ast.Type type = this.getParent().getType().asMapping().getKeyType();
				result = this.guessRawValueType(new DataWord(this.id), type, () -> this.id.getBytes()).toString();
			}
			return result;
		}

		@Override
		public DataWord getStorageValue(Function<DataWord, DataWord> valueExtractor) {
			if (this.type.isContainer()) {
				throw new UnsupportedOperationException("Cannot extract storage value for container element.");
			}
			DataWord value = null;
			StorageDictionary.PathElement pe = this.toDictionaryPathElement();
			if (Objects.nonNull(pe)) {
				value = valueExtractor.apply(new DataWord(pe.storageKey));
				if (Objects.nonNull(this.member)) {
					value = this.member.extractValue(value);
				} else {
					Member parentMember = this.getParent().getMember();
					if (parentMember.getType().isStaticArray()) {
						Ast.Type.Array array = parentMember.getType().asArray();
						if (array.getSize() > parentMember.reservedSlotsCount()) {
							int typeSize = Member.size(array.getElementType());
							int index = NumberUtils.toInt(this.id);
							value = extractPackedArrEl(value, index, typeSize);
						}
					}
				}
			}
			return value;
		}

		@Override
		public String getValue(Function<DataWord, DataWord> valueExtractor) {
			DataWord rawValue = this.getStorageValue(valueExtractor);
			Object typed = this.guessRawValueType(rawValue, this.type, () -> {
				StorageDictionary.PathElement pathElement = this.toDictionaryPathElement();
				if (pathElement == null) {
					return ArrayUtils.EMPTY_BYTE_ARRAY;
				} else {
					if (pathElement.hasChildren()) {
						byte[][] bytes = pathElement.getChildrenStream()
								.map(child -> valueExtractor.apply(new DataWord(child.storageKey)))
								.filter(Objects::nonNull).map(DataWord::getData)
								.toArray(x$0 -> new byte[x$0][]);
						if (ArrayUtils.isNotEmpty((Object[]) bytes)) {
							return ByteUtil.merge(bytes);
						}
					}
					DataWord value = valueExtractor.apply(new DataWord(pathElement.storageKey));
					return (value == null) ? ArrayUtils.EMPTY_BYTE_ARRAY : value.getData();
				}
			});
			return Objects.toString(typed, null);
		}

		private Object guessRawValueType(DataWord rawValue, Ast.Type type,
				Supplier<byte[]> bytesExtractor) {
			Object result = rawValue;
			if (type.isEnum()) {
				result = ContractData.this.getEnumValueByOrdinal(type.asEnum(),
						Objects.isNull(rawValue) ? 0 : rawValue.intValue());
			} else if (type.isContract() && Objects.nonNull(rawValue)) {
				result = ByteUtil.toHexString(rawValue.getLast20Bytes());
			} else if (type.isElementary()) {
				Ast.Type.Elementary elementary = type.asElementary();
				if (elementary.isString()) {
					byte[] bytes = bytesExtractor.get();
					bytes = ArrayUtils.subarray(bytes, 0, ArrayUtils.indexOf(bytes, (byte) 0));
					if (ArrayUtils.getLength((Object) bytes) == 32) {
						bytes = ArrayUtils.subarray(bytes, 0, 31);
					}
					result = new String(bytes);
				} else if (elementary.is("bytes")) {
					result = Hex.toHexString((byte[]) bytesExtractor.get());
				} else if (elementary.isBool()) {
					result = (!Objects.isNull(rawValue) && !rawValue.isZero());
				} else if (elementary.isAddress() && Objects.nonNull(rawValue)) {
					result = ByteUtil.toHexString(rawValue.getLast20Bytes());
				} else if (elementary.isNumber()) {
					result = (Objects.isNull(rawValue) ? 0 : rawValue.bigIntValue());
				}
			}
			return result;
		}

		public String getId() {
			return this.id;
		}

		@Override
		public Ast.Type getType() {
			return this.type;
		}

		@Override
		public Element getParent() {
			return this.parent;
		}

		public Member getMember() {
			return this.member;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof ElementImpl)) {
				return false;
			}
			ElementImpl other = (ElementImpl) o;
			if (!other.canEqual(this)) {
				return false;
			}
			Object this$id = this.getId();
			Object other$id = other.getId();
			Label_0065: {
				if (this$id == null) {
					if (other$id == null) {
						break Label_0065;
					}
				} else if (this$id.equals(other$id)) {
					break Label_0065;
				}
				return false;
			}
			Object this$parent = this.getParent();
			Object other$parent = other.getParent();
			if (this$parent == null) {
				if (other$parent == null) {
					return true;
				}
			} else if (this$parent.equals(other$parent)) {
				return true;
			}
			return false;
		}

		protected boolean canEqual(Object other) {
			return other instanceof ElementImpl;
		}

		@Override
		public int hashCode() {
			int PRIME = 59;
			int result = 1;
			Object $id = this.getId();
			result = result * PRIME + (($id == null) ? 0 : $id.hashCode());
			Object $parent = this.getParent();
			result = result * PRIME + (($parent == null) ? 0 : $parent.hashCode());
			return result;
		}
	}
}