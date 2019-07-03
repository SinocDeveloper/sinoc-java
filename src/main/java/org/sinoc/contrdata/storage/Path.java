package org.sinoc.contrdata.storage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinoc.contrdata.contract.Ast;
import org.sinoc.contrdata.contract.ContractData;
import org.sinoc.contrdata.contract.Member;
import org.sinoc.contrdata.contract.Members;

public class Path extends ArrayList<String>
{
	private static final long serialVersionUID = 1L;
	private static final String SEPARATOR = "|";
    
    public Path() {
        this(new ArrayList<Object>());
    }
    
    public Path(final List<?> path) {
        super(path.stream().map(Object::toString).collect(Collectors.toList()));
    }
    
    public String[] parts() {
        return this.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
    
    public Path extend(final Object... extraParts) {
        return of(Stream.concat(this.stream(), (Stream<?>)Arrays.stream(extraParts)).toArray());
    }
    
    public String first() {
        return this.isEmpty() ? null : this.get(0);
    }
    
    public String last() {
        return this.isEmpty() ? null : this.get(this.size() - 1);
    }
    
    public String removeLast() {
        if (this.isEmpty()) {
            throw new IllegalStateException("Can not remove last item: path is empty.");
        }
        return this.remove(this.size() - 1);
    }
    
    public Path tail() {
        return (this.size() > 1) ? new Path(this.subList(1, this.size())) : empty();
    }
    
    @Override
    public String toString() {
        return StringUtils.join(this, SEPARATOR);
    }
    
    public static Path empty() {
        return new Path(Collections.emptyList());
    }
    
    public static Path of(final Object... parts) {
        return new Path(Arrays.asList(parts));
    }
    
    public static Path parse(final String path) {
        return of((Object[])StringUtils.split(StringUtils.defaultString(path), "\\|"));
    }
    
    public static Path parseHumanReadable(final String path, final ContractData contractData) {
        final Path humanReadable = of((Object[])path.split("(\\[|(\\]\\[)|(\\]\\.?))|\\."));
        return fromHumanReadable(humanReadable, null, contractData.getMembers(), contractData, new Path());
    }
    
    private static Path fromHumanReadable(final Path humanReadable, Ast.Type type, Members members, final ContractData contractData, final Path result) {
        String part = humanReadable.first();
        if (Objects.isNull(part)) {
            return result;
        }
        if (Objects.isNull(type)) {
            final Member member = members.findByName(part);
            part = String.valueOf(member.getPosition());
            type = member.getType();
        }
        else if (type.isMapping()) {
            type = type.asMapping().getValueType();
        }
        else if (type.isArray()) {
            type = type.asArray().getElementType();
        }
        if (type.isStruct()) {
            final Ast.Type.Struct struct = type.asStruct();
            members = contractData.getStructFields(struct);
            type = null;
        }
        return fromHumanReadable(humanReadable.tail(), type, members, contractData, result.extend(part));
    }
}
