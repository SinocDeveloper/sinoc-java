package org.sinoc.contrdata.storage.dictionary;


import org.sinoc.datasource.*;
import org.sinoc.vm.*;
import org.sinoc.datasource.inmem.*;
import org.sinoc.db.*;
import org.spongycastle.util.encoders.*;
import java.util.*;
import com.fasterxml.jackson.annotation.*;
import java.math.*;
import java.util.stream.*;
import org.apache.commons.lang3.*;
import org.sinoc.util.*;

public class StorageDictionary
{
    private static int MAX_CHILDREN_TO_SORT = 100;
    //private static boolean SORT_MAP_KEYS = false;
    private Source<byte[], byte[]> storageDb;
    private PathElement root;
    private boolean exist;
    private Map<ByteArrayWrapper, PathElement> cache;
    private List<PathElement> dirtyNodes;
    
    public static PathElement[] emptyPathElements() {
        return pathElements(new PathElement[0]);
    }
    
    public static PathElement[] pathElements(PathElement... elements) {
        return elements;
    }
    
    private PathElement get(byte[] hash) {
        if (hash == null) {
            return null;
        }
        PathElement ret = this.cache.get(new ByteArrayWrapper(hash));
        if (ret == null) {
            ret = this.load(hash);
            if (ret != null) {
                this.put(ret);
            }
        }
        return ret;
    }
    
    private void put(PathElement pe) {
        this.cache.put(new ByteArrayWrapper(pe.storageKey), pe);
        pe.dictionary = this;
    }
    
    public PathElement load(byte[] hash) {
        PathElement element = null;
        byte[] bytes = (byte[])this.storageDb.get(hash);
        if (ArrayUtils.isNotEmpty(bytes)) {
            element = PathElement.deserialize(bytes);
            element.setDictionary(this);
        }
        return element;
    }
    
    public void store() {
        this.dirtyNodes.stream().forEach(node -> this.storageDb.put(node.getHash(), node.serialize()));
        this.dirtyNodes.clear();
    }
    
    public StorageDictionary getFiltered(Set<DataWord> hashFilter) {
        StorageDictionary result = new StorageDictionary(new HashMapDB<>());
        for (DataWord hash : hashFilter) {
            List<PathElement> path = new ArrayList<PathElement>();
            PathElement pathElement = this.get(hash.getData());
            if (pathElement == null) {
                continue;
            }
            while (!pathElement.is(PathElement.Type.Root)) {
                path.add(0, pathElement.copyLight());
                pathElement = pathElement.getParent();
            }
            result.addPath(path.stream().toArray(PathElement[]::new));
        }
        return result;
    }
    
    public StorageDictionary(Source<byte[], byte[]> storageDb) {
        this.cache = new HashMap<ByteArrayWrapper, PathElement>();
        this.dirtyNodes = new ArrayList<PathElement>();
        this.storageDb = storageDb;
        this.root = this.load(PathElement.rootHash);
        if (!(this.exist = (this.root != null))) {
            this.put(this.root = PathElement.createRoot());
        }
    }
    
    public boolean isExist() {
        return this.exist;
    }
    
    public boolean hasChanges() {
        return !this.dirtyNodes.isEmpty();
    }
    
    public synchronized void addPath(PathElement[] path) {
        int startIdx = path.length - 1;
        PathElement existingPE = null;
        while (startIdx >= 0 && !Objects.nonNull(existingPE = this.get(path[startIdx].getHash()))) {
            --startIdx;
        }
        existingPE = ((startIdx >= 0) ? existingPE : this.root);
        ++startIdx;
        existingPE.addChildPath(Arrays.copyOfRange(path, startIdx, path.length));
    }
    
    public String dump(ContractDetails storage) {
        return this.root.toString(storage, 0);
    }
    
    public String dump() {
        return this.dump(null);
    }
    
    public static void dmp(PathElement el, Map<String, String> dump) {
        dump.put(Hex.toHexString(el.getHash()), Hex.toHexString(el.serialize()));
        el.getChildrenStream().forEach(child -> dmp(child, dump));
    }
    
    public Map<String, String> dmp() {
        Map<String, String> result = new HashMap<String, String>();
        dmp(this.root, result);
        return result;
    }
    
    public static StorageDictionary readDmp(Map<String, String> dump) {
        HashMapDB<byte[]> storageDb = new HashMapDB<>();
        dump.entrySet().stream().forEach(entry -> storageDb.put(Hex.decode(entry.getKey()), Hex.decode(entry.getValue())));
        return new StorageDictionary((Source<byte[], byte[]>)storageDb);
    }
    
    public PathElement getByPath(String... path) {
        PathElement result = this.root;
        for (String pathPart : path) {
            if (result == null) {
                return null;
            }
            result = result.findChildByKey(pathPart);
        }
        return result;
    }
    
    public Set<DataWord> allKeys() {
        return this.findKeysIn(this.root, new HashSet<DataWord>());
    }
    
    private Set<DataWord> findKeysIn(PathElement parent, Set<DataWord> keys) {
        parent.getChildren().forEach(child -> {
            if (child.hasChildren()) {
                this.findKeysIn(child, keys);
            }
            else {
                keys.add(new DataWord(child.storageKey));
            }
            return;
        });
        return keys;
    }
    
    public Source<byte[], byte[]> getStorageDb() {
        return this.storageDb;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class PathElement implements Comparable<PathElement>
    {
        private StorageDictionary dictionary;
        @JsonProperty
        public Type type;
        @JsonProperty
        public String key;
        @JsonProperty
        public byte[] storageKey;
        @JsonProperty
        public Boolean childrenCompacted;
        @JsonProperty
        public int childrenCount;
        @JsonProperty
        public byte[] parentHash;
        @JsonProperty
        public byte[] nextSiblingHash;
        @JsonProperty
        public byte[] firstChildHash;
        @JsonProperty
        public byte[] lastChildHash;
        private static byte[] rootHash;
        private static String NO_OFFSET_KEY = "0";
        
        public PathElement() {
            this.childrenCompacted = null;
            this.childrenCount = 0;
        }
        
        public PathElement(Type type, String key, byte[] storageKey) {
            this.childrenCompacted = null;
            this.childrenCount = 0;
            this.type = type;
            this.key = key;
            this.storageKey = storageKey;
        }
        
        private static PathElement createRoot() {
            return new PathElement(Type.Root, NO_OFFSET_KEY, PathElement.rootHash);
        }
        
        public PathElement(Type type, int indexOffset, byte[] storageKey) {
            this(type, String.valueOf(indexOffset), storageKey);
        }
        
        public static PathElement createMapKey(String key, byte[] storageKey) {
            return new PathElement(Type.MapKey, key, storageKey);
        }
        
        public static PathElement createMapKey(int key, byte[] storageKey) {
            return createMapKey(String.valueOf(key), storageKey);
        }
        
        public PathElement clone() {
            PathElement result = new PathElement();
            result.dictionary = this.dictionary;
            result.type = this.type;
            result.key = this.key;
            result.storageKey = this.storageKey;
            result.childrenCompacted = this.childrenCompacted;
            result.childrenCount = this.childrenCount;
            result.parentHash = this.parentHash;
            result.nextSiblingHash = this.nextSiblingHash;
            result.firstChildHash = this.firstChildHash;
            result.lastChildHash = this.lastChildHash;
            return result;
        }
        
        public boolean is(Type type) {
            return this.type == type;
        }
        
        public PathElement getParent() {
            return this.dictionary.get(this.parentHash);
        }
        
        public PathElement getFirstChild() {
            return this.dictionary.get(this.firstChildHash);
        }
        
        public PathElement getLastChild() {
            return this.dictionary.get(this.lastChildHash);
        }
        
        public PathElement getNextSibling() {
            return this.dictionary.get(this.nextSiblingHash);
        }
        
        public boolean hasChild(PathElement pathElement) {
            return pathElement != null && Arrays.equals(this.storageKey, pathElement.parentHash);
        }
        
        public PathElement addChild(PathElement newChild) {
            PathElement existingChild = this.dictionary.get(newChild.storageKey);
            if (this.hasChild(existingChild)) {
                return existingChild;
            }
            if (this.childrenCount > MAX_CHILDREN_TO_SORT || newChild.is(Type.MapKey)) {
                this.insertChild(this.getLastChild(), newChild);
            }
            else {
                PathElement insertAfter = null;
                Iterator<PathElement> chIt = this.getChildrenIterator();
                List<PathElement> prevs = new ArrayList<PathElement>();
                boolean hasLoop = false;
                while (chIt.hasNext()) {
                    PathElement next = chIt.next();
                    for (PathElement prev : prevs) {
                        hasLoop = (prev.compareTo(next) == 0);
                        if (hasLoop) {
                            break;
                        }
                    }
                    if (hasLoop) {
                        break;
                    }
                    prevs.add(next);
                    if (newChild.compareTo(next) < 0) {
                        break;
                    }
                    insertAfter = next;
                }
                this.insertChild(insertAfter, newChild);
            }
            return newChild;
        }
        
        public PathElement insertChild(PathElement insertAfter, PathElement newChild) {
            if (insertAfter == null) {
                newChild.nextSiblingHash = this.firstChildHash;
                this.firstChildHash = newChild.storageKey;
                if (this.childrenCount == 0) {
                    this.lastChildHash = this.firstChildHash;
                }
            }
            else if (insertAfter.nextSiblingHash == null) {
                insertAfter.nextSiblingHash = newChild.storageKey;
                insertAfter.invalidate();
                this.lastChildHash = newChild.storageKey;
            }
            else {
                newChild.nextSiblingHash = insertAfter.nextSiblingHash;
                insertAfter.nextSiblingHash = newChild.storageKey;
                insertAfter.invalidate();
            }
            newChild.parentHash = this.storageKey;
            this.dictionary.put(newChild);
            newChild.invalidate();
            ++this.childrenCount;
            this.invalidate();
            return newChild;
        }
        
        public void addChildPath(PathElement[] pathElements) {
            if (pathElements.length == 0) {
                return;
            }
            boolean addCompacted;
            if (pathElements.length > 1 && pathElements[1].canBeCompactedWithParent()) {
                if (this.childrenCompacted == Boolean.FALSE) {
                    addCompacted = false;
                }
                else {
                    this.childrenCompacted = Boolean.TRUE;
                    addCompacted = true;
                }
            }
            else {
                addCompacted = false;
                if (this.childrenCompacted == Boolean.TRUE) {
                    this.childrenCompacted = Boolean.FALSE;
                    this.decompactAllChildren();
                }
                else {
                    this.childrenCompacted = Boolean.FALSE;
                }
            }
            if (addCompacted) {
                PathElement compacted = compactPath(pathElements[0], pathElements[1]);
                PathElement child = this.addChild(compacted);
                child.addChildPath(Arrays.copyOfRange(pathElements, 2, pathElements.length));
                this.dictionary.put(compacted);
            }
            else {
                PathElement child2 = this.addChild(pathElements[0]);
                child2.addChildPath(Arrays.copyOfRange(pathElements, 1, pathElements.length));
            }
        }
        
        private static PathElement compactPath(PathElement parent, PathElement child) {
            return new PathElement(parent.type, parent.key, child.storageKey);
        }
        
        private PathElement[] decompactElement(PathElement pe) {
            PathElement parent = new PathElement(pe.type, pe.key, toVirtualStorageKey(pe.storageKey));
            this.dictionary.put(parent);
            PathElement child = new PathElement(Type.Offset, "0", pe.storageKey);
            child.childrenCount = pe.childrenCount;
            child.firstChildHash = pe.firstChildHash;
            child.lastChildHash = pe.lastChildHash;
            this.dictionary.put(child);
            return new PathElement[] { parent, child };
        }
        
        private void decompactAllChildren() {
            PathElement child = this.getFirstChild();
            this.removeAllChildren();
            while (child != null) {
                this.addChildPath(this.decompactElement(child));
                child = child.getNextSibling();
            }
        }
        
        private void removeAllChildren() {
            this.childrenCount = 0;
            this.firstChildHash = null;
            this.lastChildHash = null;
        }
        
        public static byte[] toVirtualStorageKey(byte[] childStorageKey) {
            BigInteger i = ByteUtil.bytesToBigInteger(childStorageKey).subtract(BigInteger.ONE);
            return ByteUtil.bigIntegerToBytes(i, 32);
        }
        
        private boolean canBeCompactedWithParent() {
            return this.is(Type.Offset) && "0".equals(this.key);
        }
        
        public Iterator<PathElement> getChildrenIterator() {
            return new Iterator<PathElement>() {
                private PathElement cur = PathElement.this.getFirstChild();
                
                @Override
                public boolean hasNext() {
                    return this.cur != null;
                }
                
                @Override
                public PathElement next() {
                    PathElement ret = this.cur;
                    this.cur = this.cur.getNextSibling();
                    return ret;
                }
            };
        }
        
        public Stream<PathElement> getChildrenStream() {
            return StreamSupport.stream(this.getChildren().spliterator(), false);
        }
        
        public Iterable<PathElement> getChildren() {
            return () -> this.getChildrenIterator();
        }
        
        public List<PathElement> getChildren(int offset, int count) {
            List<PathElement> result = new ArrayList<PathElement>();
            int i = 0;
            for (PathElement child : this.getChildren()) {
                if (offset <= i++) {
                    result.add(child);
                }
                if (result.size() == count) {
                    return result;
                }
            }
            return result;
        }
        
        public PathElement findChildByKey(String key) {
            Iterator<PathElement> children = this.getChildrenIterator();
            while (children.hasNext()) {
                PathElement child = children.next();
                if (StringUtils.equals((CharSequence)child.key, (CharSequence)key)) {
                    return child;
                }
            }
            return null;
        }
        
        public boolean hasChildren() {
            return this.getChildrenCount() > 0;
        }
        
        public byte[] getHash() {
            return this.storageKey;
        }
        
        private void invalidate() {
            this.dictionary.dirtyNodes.add(this);
        }
        
        public int getChildrenCount() {
            return this.childrenCount;
        }
        
        public String[] getFullPath() {
            return (String[])(this.is(Type.Root) ? ArrayUtils.EMPTY_STRING_ARRAY : Utils.mergeArrays((Object[][])new String[][] { this.getParent().getFullPath(), { this.key } }));
        }
        
        @Override
        public int compareTo(PathElement o) {
            if (this.type != o.type) {
                return this.type.compareTo(o.type);
            }
            if (!this.is(Type.Offset) && !this.is(Type.StorageIndex)) {
                if (!this.is(Type.ArrayIndex)) {
                    return this.key.compareTo(o.key);
                }
            }
            try {
                return new BigInteger(this.key, 16).compareTo(new BigInteger(o.key, 16));
            }
            catch (NumberFormatException ex) {}
            return this.key.compareTo(o.key);
        }
        
        @Override
        public int hashCode() {
            int result = this.type.hashCode();
            result = 31 * result + ((this.key != null) ? this.key.hashCode() : 0);
            return result;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            PathElement that = (PathElement)o;
            if (this.type != that.type) {
                return false;
            }
            if (this.key != null) {
                if (!this.key.equals(that.key)) {
                    return false;
                }
            }
            else if (that.key != null) {
                return false;
            }
            return true;
        }
        
        private static String shortHash(byte[] hash) {
            return ArrayUtils.isEmpty(hash) ? "" : StringUtils.substring(Hex.toHexString(hash), 0, 8);
        }
        
        public String dump() {
            return this.toString() + "(storageKey=" + shortHash(this.storageKey) + ", childCount=" + this.childrenCount + ", childrenCompacted=" + this.childrenCompacted + ", parentHash=" + shortHash(this.parentHash) + ", firstChildHash=" + shortHash(this.firstChildHash) + ", lastChildHash=" + shortHash(this.lastChildHash) + ", nextSiblingHash=" + shortHash(this.nextSiblingHash) + ")";
        }
        
        @Override
        public String toString() {
            switch (this.type) {
                case Root: {
                    return "ROOT";
                }
                case StorageIndex: {
                    return "." + this.key;
                }
                case Offset: {
                    return "+" + this.key;
                }
                case MapKey: {
                    return "('" + this.key + "')";
                }
                default: {
                    return "[" + this.key + "]";
                }
            }
        }
        
        public String toString(ContractDetails storage, int indent) {
            String s = (ArrayUtils.isEmpty(this.storageKey) ? StringUtils.repeat(" ", 64) : Hex.toHexString(this.storageKey)) + " : " + StringUtils.repeat("  ", indent) + this;
            if (this.storageKey != null && storage != null) {
                DataWord data = storage.get(new DataWord(this.storageKey));
                s = s + " = " + ((data == null) ? "<null>" : GuessUtils.guessValue(data.getData()));
            }
            s += "\n";
            if (this.getChildrenCount() > 0) {
                int limit = 50;
                for (PathElement child : this.getChildren()) {
                    s += child.toString(storage, indent + 1);
                    if (limit-- <= 0) {
                        s = s + "\n             [Total: " + this.getChildrenCount() + " Rest skipped]\n";
                        break;
                    }
                }
            }
            return s;
        }
        
        public byte[] serialize() {
            byte[][] array = new byte[9][];
            array[0] = RLP.encodeInt(this.type.ordinal());
            array[1] = RLP.encodeString(this.key);
            array[2] = RLP.encodeElement(ArrayUtils.nullToEmpty(this.storageKey));
            int n = 3;
            byte[] array2;
            if (this.childrenCompacted == null) {
            	array2 = ArrayUtils.EMPTY_BYTE_ARRAY;
            }
            else if (this.childrenCompacted) {
                array2 = new byte[] { 1 };
            }
            else {
            	array2 = new byte[] { 0 };
            }
            array[n] = RLP.encodeElement(array2);
            array[4] = RLP.encodeInt(this.childrenCount);
            array[5] = RLP.encodeElement(ArrayUtils.nullToEmpty(this.parentHash));
            array[6] = RLP.encodeElement(ArrayUtils.nullToEmpty(this.nextSiblingHash));
            array[7] = RLP.encodeElement(ArrayUtils.nullToEmpty(this.firstChildHash));
            array[8] = RLP.encodeElement(ArrayUtils.nullToEmpty(this.lastChildHash));
            return RLP.encodeList(array);
        }
        
        public static PathElement deserialize(byte[] bytes) {
            PathElement result = new PathElement();
            RLPList list = (RLPList)RLP.decode2(bytes).get(0);
            result.type = Type.values()[ByteUtil.byteArrayToInt(((RLPElement)list.get(0)).getRLPData())];
            result.key = new String(((RLPElement)list.get(1)).getRLPData());
            result.storageKey = ((RLPElement)list.get(2)).getRLPData();
            byte[] compB = ((RLPElement)list.get(3)).getRLPData();
            result.childrenCompacted = ((compB == null) ? null : ((compB[0] == 0) ? Boolean.FALSE : Boolean.TRUE));
            result.childrenCount = ByteUtil.byteArrayToInt(((RLPElement)list.get(4)).getRLPData());
            result.parentHash = ((RLPElement)list.get(5)).getRLPData();
            result.nextSiblingHash = ((RLPElement)list.get(6)).getRLPData();
            result.firstChildHash = ((RLPElement)list.get(7)).getRLPData();
            result.lastChildHash = ((RLPElement)list.get(8)).getRLPData();
            return result;
        }
        
        PathElement copyLight() {
            PathElement ret = new PathElement();
            ret.type = this.type;
            ret.key = this.key;
            ret.storageKey = this.storageKey;
            return ret;
        }
        
        public void setDictionary(StorageDictionary dictionary) {
            this.dictionary = dictionary;
        }
        
        static {
            rootHash = Hex.decode("cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
        }
        
        public enum Type
        {
            Root, 
            StorageIndex, 
            Offset, 
            ArrayIndex, 
            MapKey;
        }
    }
}