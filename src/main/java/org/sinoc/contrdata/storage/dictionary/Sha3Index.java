package org.sinoc.contrdata.storage.dictionary;

import org.sinoc.vm.*;
import org.sinoc.db.*;
import java.util.stream.*;
import java.util.*;
import org.sinoc.util.*;
import org.spongycastle.util.encoders.*;
import org.sinoc.crypto.*;

public class Sha3Index
{
    private static Sha3Index calculated;
    private Map<Sha3Output, Entry> idx;
    
    public Sha3Index() {
        this.idx = new HashMap<Sha3Output, Entry>();
    }
    
    protected void add(int i) {
        byte[] input = new DataWord(i).getData();
        Sha3Output output = Sha3Output.calc(input);
        this.idx.put(output, new Entry(output.getData(), input));
    }
    
    public boolean contains(byte[] decoded) {
        ByteArrayWrapper input = new ByteArrayWrapper(decoded);
        return Stream.concat((Stream<?>)Sha3Index.calculated.idx.values().stream(), (Stream<?>)this.idx.values().stream()).filter(entry -> entry.equals(input)).findFirst().isPresent();
    }
    
    public void add(byte[] input) {
        if (this.contains(input)) {
            return;
        }
        Sha3Output output = Sha3Output.calc(input);
        this.idx.put(output, new Entry(output.getData(), input));
    }
    
    public Entry get(byte[] encoded) {
        Sha3Output output = new Sha3Output(encoded);
        Entry entry = Sha3Index.calculated.idx.get(output);
        if (Objects.isNull(entry)) {
            entry = this.idx.get(output);
        }
        return entry;
    }
    
    public void clear() {
        this.idx.clear();
    }
    
    public Collection<Entry> entries() {
        return this.idx.values();
    }
    
    public int size() {
        return this.idx.size();
    }
    
    static {
        calculated = new Sha3Index() {
            {
                IntStream.range(0, 1000).forEach(this::add);
            }
        };
    }
    
    public static class Entry
    {
        private byte[] output;
        private byte[] input;
        private int hashCode;
        
        @Override
        public int hashCode() {
            if (Objects.isNull(this.hashCode())) {
                this.hashCode = Arrays.hashCode(this.input);
            }
            return this.hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                return this.compareInput(((Entry)obj).getInput());
            }
            return obj instanceof ByteArrayWrapper && this.compareInput(((ByteArrayWrapper)obj).getData());
        }
        
        private boolean compareInput(byte[] otherInput) {
            return FastByteComparisons.compareTo(this.input, 0, this.input.length, otherInput, 0, otherInput.length) == 0;
        }
        
        @Override
        public String toString() {
            return String.format("sha3('%s') = '%s'", Hex.toHexString(this.input), Hex.toHexString(this.output));
        }
        
        public Entry(byte[] output, byte[] input) {
            this.output = output;
            this.input = input;
        }
        
        public byte[] getOutput() {
            return this.output;
        }
        
        public byte[] getInput() {
            return this.input;
        }
    }
    
    private static class Sha3Output
    {
        private static int COMPARISON_DATA_LEN = 20;
        private byte[] data;
        private int hashCode;
        
        @Override
        public int hashCode() {
            if (Objects.isNull(this.hashCode)) {
                this.hashCode = Arrays.hashCode(Arrays.copyOf(this.data, COMPARISON_DATA_LEN));
            }
            return this.hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Sha3Output) {
                byte[] otherData = ((Sha3Output)obj).getData();
                return FastByteComparisons.compareTo(this.data, 0, 20, otherData, 0, 20) == 0;
            }
            return false;
        }
        
        @Override
        public String toString() {
            return Hex.toHexString(this.data);
        }
        
        static Sha3Output calc(byte[] input) {
            return wrap(HashUtil.sha3(input));
        }
        
        private Sha3Output(byte[] data) {
            this.data = data;
        }
        
        private static Sha3Output wrap(byte[] data) {
            return new Sha3Output(data);
        }
        
        public byte[] getData() {
            return this.data;
        }
    }
}