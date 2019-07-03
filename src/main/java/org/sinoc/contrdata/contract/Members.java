package org.sinoc.contrdata.contract;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class Members extends ArrayList<Member>
{
	private static final long serialVersionUID = 1L;

	public Members() {
    }
    
    public Members(List<Member> members) {
        super(members);
    }
    
    private Member findBy(Predicate<Member> predicate) {
        return this.stream().filter(predicate).findFirst().orElse(null);
    }
    
    public Member findByName(String name) {
        return this.findBy(member -> StringUtils.equals((CharSequence)member.getName(), (CharSequence)name));
    }
    
    public Member findByPosition(int position) {
        return this.findBy(member -> member.getPosition() == position);
    }
    
    public int reservedSlotsCount() {
        return this.stream().mapToInt(Member::reservedSlotsCount).sum();
    }
    
    public Members filter(Predicate<? super Member> predicate) {
        return new Members((List<Member>)this.stream().filter(predicate).collect(Collectors.toList()));
    }
    
    public Members page(int page, int size) {
        int offset = page * size;
        int fromIndex = Math.max(offset, 0);
        int toIndex = Math.min(this.size(), offset + size);
        return new Members((fromIndex < toIndex) ? this.subList(fromIndex, toIndex) : Collections.emptyList());
    }
    
    private static Members of(ContractData contractData, Ast.Entries<Ast.Variable> variables) {
        Members members = new Members();
        ArrayList<Member> list = new ArrayList<>();
        variables.stream().forEach(var -> {
        	Member last = (list.isEmpty() ? null : list.get(list.size() - 1));
            list.add(new Member(last, var, contractData));
            return;
        });
        return members;
    }
    
    public static Members ofContract(ContractData contractData) {
        return of(contractData, contractData.getContract().getVariables());
    }
    
    public static Members ofStructure(ContractData contractData, Ast.Structure structure) {
        return of(contractData, structure.getVariables());
    }
}
