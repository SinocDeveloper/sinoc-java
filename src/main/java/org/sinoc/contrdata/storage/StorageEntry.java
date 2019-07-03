package org.sinoc.contrdata.storage;

import org.apache.commons.collections4.keyvalue.*;
import org.sinoc.contrdata.contract.*;
import org.sinoc.contrdata.storage.dictionary.*;
import org.sinoc.vm.*;
import java.util.function.*;
import org.sinoc.util.*;
import java.util.*;

public class StorageEntry extends AbstractKeyValue<Object, Object> implements Comparable<StorageEntry>
{
    private Type type;
    
    private StorageEntry(Type type, Object key, Object value) {
        super(key, value);
        this.type = type;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public int compareTo(StorageEntry another) {
        Object key = this.getKey();
        if (key instanceof Comparable) {
            return ((Comparable)key).compareTo(another.getKey());
        }
        return 0;
    }
    
    private static String resolveKind(ContractData.Element el) {
        String result = null;
        if (el.getParent().isRoot()) {
            result = "data member";
        }
        else {
            Ast.Type prevElType = el.getParent().getType();
            if (prevElType.isArray()) {
                result = "index";
            }
            else if (prevElType.isStruct()) {
                result = "field";
            }
            else if (prevElType.isMapping()) {
                result = prevElType.asMapping().getKeyType().formatName();
            }
        }
        return result;
    }
    
    private static String resolveKeyKind(StorageDictionary.PathElement element) {
        switch (element.type) {
            case StorageIndex:
            case MapKey: {
                return "key";
            }
            case ArrayIndex: {
                return "index";
            }
            case Offset: {
                return "offset";
            }
            default: {
                return "";
            }
        }
    }
    
    private static String resolveValueKind(StorageDictionary.PathElement element) {
        String result = "data";
        if (element.hasChildren()) {
            switch (element.getFirstChild().type) {
                case MapKey: {
                    result = "map";
                    break;
                }
                case ArrayIndex: {
                    result = "array";
                    break;
                }
                case Offset: {
                    result = "struct";
                    break;
                }
                default:
                	result = "data";
                	break;
            }
        }
        return result;
    }
    
    public static StorageEntry raw(Map.Entry<DataWord, DataWord> entry) {
        return new StorageEntry(Type.raw, entry.getKey(), entry.getValue());
    }
    
    public static StorageEntry structured(StorageDictionary.PathElement pe, Function<DataWord, DataWord> valueExtractor) {
        Key.KeyBuilder key = Key.builder().kind(resolveKeyKind(pe)).encoded(ByteUtil.toHexString(pe.storageKey)).decoded(pe.key).path(Path.of((Object[])pe.getFullPath()).toString());
        Value.ValueBuilder value = Value.builder().typeKind(resolveValueKind(pe)).container(pe.hasChildren()).size(pe.getChildrenCount());
        if (!pe.hasChildren()) {
            DataWord storageValue = valueExtractor.apply(new DataWord(pe.storageKey));
            value.encoded(Objects.toString(storageValue, null));
        }
        return new StorageEntry(Type.structured, key.build(), value.build());
    }
    
    public static StorageEntry smart(ContractData.Element cde, Function<DataWord, DataWord> valueExtractor) {
        StorageDictionary.PathElement pathElement = cde.toDictionaryPathElement();
        Key.KeyBuilder key = Key.builder().kind(resolveKind(cde)).encoded((pathElement == null) ? null : ByteUtil.toHexString(pathElement.storageKey)).decoded(cde.getKey()).path(cde.path().toString());
        Ast.Type type = cde.getType();
        Value.ValueBuilder value = Value.builder().type(type.formatName()).typeKind(type.isUserDefined() ? type.getName() : null).container(type.isContainer());
        if (type.isContainer()) {
            value.size(cde.getChildrenCount());
        }
        else if (!type.isStruct()) {
            value.encoded(Objects.toString(cde.getStorageValue(valueExtractor), null)).decoded(cde.getValue(valueExtractor));
        }
        return new StorageEntry(Type.smart, key.build(), value.build());
    }
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StorageEntry)) {
            return false;
        }
        StorageEntry other = (StorageEntry)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Object this$type = this.getType();
        Object other$type = other.getType();
        if (this$type == null) {
            if (other$type == null) {
                return true;
            }
        }
        else if (this$type.equals(other$type)) {
            return true;
        }
        return false;
    }
    
    protected boolean canEqual(Object other) {
        return other instanceof StorageEntry;
    }
    
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $type = this.getType();
        result = result * PRIME + (($type == null) ? 0 : $type.hashCode());
        return result;
    }
    
    public Type getType() {
        return this.type;
    }
    
    public static class Key implements Comparable<Key>
    {
        private String encoded;
        private String decoded;
        private String kind;
        private String path;
        
        @Override
        public int compareTo(Key another) {
            return this.encoded.compareTo(another.getEncoded());
        }
        
        Key(String encoded, String decoded, String kind, String path) {
            this.encoded = encoded;
            this.decoded = decoded;
            this.kind = kind;
            this.path = path;
        }
        
        public static KeyBuilder builder() {
            return new KeyBuilder();
        }
        
        public String getEncoded() {
            return this.encoded;
        }
        
        public String getDecoded() {
            return this.decoded;
        }
        
        public String getKind() {
            return this.kind;
        }
        
        public String getPath() {
            return this.path;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key other = (Key)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Object this$encoded = this.getEncoded();
            Object other$encoded = other.getEncoded();
            Label_0065: {
                if (this$encoded == null) {
                    if (other$encoded == null) {
                        break Label_0065;
                    }
                }
                else if (this$encoded.equals(other$encoded)) {
                    break Label_0065;
                }
                return false;
            }
            Object this$decoded = this.getDecoded();
            Object other$decoded = other.getDecoded();
            Label_0102: {
                if (this$decoded == null) {
                    if (other$decoded == null) {
                        break Label_0102;
                    }
                }
                else if (this$decoded.equals(other$decoded)) {
                    break Label_0102;
                }
                return false;
            }
            Object this$kind = this.getKind();
            Object other$kind = other.getKind();
            Label_0139: {
                if (this$kind == null) {
                    if (other$kind == null) {
                        break Label_0139;
                    }
                }
                else if (this$kind.equals(other$kind)) {
                    break Label_0139;
                }
                return false;
            }
            Object this$path = this.getPath();
            Object other$path = other.getPath();
            if (this$path == null) {
                if (other$path == null) {
                    return true;
                }
            }
            else if (this$path.equals(other$path)) {
                return true;
            }
            return false;
        }
        
        protected boolean canEqual(Object other) {
            return other instanceof Key;
        }
        
        @Override
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $encoded = this.getEncoded();
            result = result * PRIME + (($encoded == null) ? 0 : $encoded.hashCode());
            Object $decoded = this.getDecoded();
            result = result * PRIME + (($decoded == null) ? 0 : $decoded.hashCode());
            Object $kind = this.getKind();
            result = result * PRIME + (($kind == null) ? 0 : $kind.hashCode());
            Object $path = this.getPath();
            result = result * PRIME + (($path == null) ? 0 : $path.hashCode());
            return result;
        }
        
        public static class KeyBuilder
        {
            private String encoded;
            private String decoded;
            private String kind;
            private String path;
            
            public KeyBuilder encoded(String encoded) {
                this.encoded = encoded;
                return this;
            }
            
            public KeyBuilder decoded(String decoded) {
                this.decoded = decoded;
                return this;
            }
            
            public KeyBuilder kind(String kind) {
                this.kind = kind;
                return this;
            }
            
            public KeyBuilder path(String path) {
                this.path = path;
                return this;
            }
            
            public Key build() {
                return new Key(this.encoded, this.decoded, this.kind, this.path);
            }
            
            @Override
            public String toString() {
                return "StorageEntry.Key.KeyBuilder(encoded=" + this.encoded + ", decoded=" + this.decoded + ", kind=" + this.kind + ", path=" + this.path + ")";
            }
        }
    }
    
    public static class Value
    {
        private String encoded;
        private String decoded;
        private String type;
        private String typeKind;
        private boolean container;
        private int size;
        
        Value(String encoded, String decoded, String type, String typeKind, boolean container, int size) {
            this.encoded = encoded;
            this.decoded = decoded;
            this.type = type;
            this.typeKind = typeKind;
            this.container = container;
            this.size = size;
        }
        
        public static ValueBuilder builder() {
            return new ValueBuilder();
        }
        
        public String getEncoded() {
            return this.encoded;
        }
        
        public String getDecoded() {
            return this.decoded;
        }
        
        public String getType() {
            return this.type;
        }
        
        public String getTypeKind() {
            return this.typeKind;
        }
        
        public boolean isContainer() {
            return this.container;
        }
        
        public int getSize() {
            return this.size;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Value)) {
                return false;
            }
            Value other = (Value)o;
            if (!other.canEqual(this)) {
                return false;
            }
            Object this$encoded = this.getEncoded();
            Object other$encoded = other.getEncoded();
            Label_0065: {
                if (this$encoded == null) {
                    if (other$encoded == null) {
                        break Label_0065;
                    }
                }
                else if (this$encoded.equals(other$encoded)) {
                    break Label_0065;
                }
                return false;
            }
            Object this$decoded = this.getDecoded();
            Object other$decoded = other.getDecoded();
            Label_0102: {
                if (this$decoded == null) {
                    if (other$decoded == null) {
                        break Label_0102;
                    }
                }
                else if (this$decoded.equals(other$decoded)) {
                    break Label_0102;
                }
                return false;
            }
            Object this$type = this.getType();
            Object other$type = other.getType();
            Label_0139: {
                if (this$type == null) {
                    if (other$type == null) {
                        break Label_0139;
                    }
                }
                else if (this$type.equals(other$type)) {
                    break Label_0139;
                }
                return false;
            }
            Object this$typeKind = this.getTypeKind();
            Object other$typeKind = other.getTypeKind();
            if (this$typeKind == null) {
                if (other$typeKind == null) {
                    return this.isContainer() == other.isContainer() && this.getSize() == other.getSize();
                }
            }
            else if (this$typeKind.equals(other$typeKind)) {
                return this.isContainer() == other.isContainer() && this.getSize() == other.getSize();
            }
            return false;
        }
        
        protected boolean canEqual(Object other) {
            return other instanceof Value;
        }
        
        @Override
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $encoded = this.getEncoded();
            result = result * PRIME + (($encoded == null) ? 0 : $encoded.hashCode());
            Object $decoded = this.getDecoded();
            result = result * PRIME + (($decoded == null) ? 0 : $decoded.hashCode());
            Object $type = this.getType();
            result = result * PRIME + (($type == null) ? 0 : $type.hashCode());
            Object $typeKind = this.getTypeKind();
            result = result * PRIME + (($typeKind == null) ? 0 : $typeKind.hashCode());
            result = result * PRIME + (this.isContainer() ? 79 : 97);
            result = result * PRIME + this.getSize();
            return result;
        }
        
        public static class ValueBuilder
        {
            private String encoded;
            private String decoded;
            private String type;
            private String typeKind;
            private boolean container;
            private int size;
            
            public ValueBuilder encoded(String encoded) {
                this.encoded = encoded;
                return this;
            }
            
            public ValueBuilder decoded(String decoded) {
                this.decoded = decoded;
                return this;
            }
            
            public ValueBuilder type(String type) {
                this.type = type;
                return this;
            }
            
            public ValueBuilder typeKind(String typeKind) {
                this.typeKind = typeKind;
                return this;
            }
            
            public ValueBuilder container(boolean container) {
                this.container = container;
                return this;
            }
            
            public ValueBuilder size(int size) {
                this.size = size;
                return this;
            }
            
            public Value build() {
                return new Value(this.encoded, this.decoded, this.type, this.typeKind, this.container, this.size);
            }
            
            @Override
            public String toString() {
                return "StorageEntry.Value.ValueBuilder(encoded=" + this.encoded + ", decoded=" + this.decoded + ", type=" + this.type + ", typeKind=" + this.typeKind + ", container=" + this.container + ", size=" + this.size + ")";
            }
        }
    }
    
    public enum Type
    {
        raw, 
        structured, 
        smart;
    }
}
