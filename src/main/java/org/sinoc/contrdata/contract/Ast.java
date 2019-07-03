package org.sinoc.contrdata.contract;

import java.util.stream.*;
import java.util.regex.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.*;
import org.apache.commons.lang3.*;
import com.fasterxml.jackson.annotation.*;
import java.io.*;
import java.util.function.*;
import java.util.*;
import org.apache.commons.lang3.math.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Ast {
	private Root root;

	public Ast() {
		this.root = new Root();
	}

	public Contract getContractAllDataMembers(final String name) {
		final List<Contract> hierarchy = this.root.getContractHierarchy(name);
		final List<Variable> variables = hierarchy.stream().flatMap(parent -> parent.getVariables().stream())
				.filter(var -> !var.isConstant())
				.collect(Collectors.toList());
		
		final List<Structure> structures = hierarchy.stream().flatMap(parent -> parent.getStructures().stream())
				.collect(Collectors.toList());
		final List<Enum> enums = hierarchy.stream().flatMap(parent -> parent.getEnums().stream())
				.collect(Collectors.toList());
		final Contract contract = new Contract(this.root, name);
		contract.getVariables().addAll(variables);
		contract.getStructures().addAll(structures);
		contract.getEnums().addAll(enums);
		return contract;
	}

	private static Ast parse(final Scanner scanner) {
		final Ast result = new Ast();
		try {
			Throwable t = null;
			try {
				while (scanner.hasNextLine()) {
					final Line line = new Line(scanner.nextLine());
					result.root.apply(line);
				}
			} catch (Throwable t2) {
				t = t2;
				throw t2;
			} finally {
				if (scanner != null) {
					if (t != null) {
						try {
							scanner.close();
						} catch (Throwable t3) {
							t.addSuppressed(t3);
						}
					} else {
						scanner.close();
					}
				}
			}
		} finally {
			result.root.resolveDeferredTypeDefinitions();
		}
		return result;
	}

	public static Ast parse(final InputStream inputStream) {
		return parse(new Scanner(inputStream));
	}

	public static Ast parse(final String rawAst) {
		return parse(new Scanner(rawAst));
	}

	public Root getRoot() {
		return this.root;
	}

	private static class Patterns {
		public static final Pattern LEVEL_DETECTOR;
		public static final Pattern CONTRACT_DEFINITION;
		public static final Pattern STRUCTURE_DEFINITION;
		public static final Pattern VARIABLE_DECLARATION;
		public static final Pattern ENUM_DEFINITION;
		public static final Pattern ENUM_VALUE;
		public static final Pattern ELEMENTARY_TYPE_NAME;
		public static final Pattern USER_DEFINED_TYPE_NAME;
		public static final Pattern CONSTANT_DETECTOR;
		public static final Pattern INHERITANCE_SPECIFIER;
		public static final Pattern LITERAL_DETECTOR;

		static {
			LEVEL_DETECTOR = Pattern.compile("(\\s*)(.+)");
			CONTRACT_DEFINITION = Pattern.compile("ContractDefinition \"(.+)\"");
			STRUCTURE_DEFINITION = Pattern.compile("StructDefinition \"(.+)\"");
			VARIABLE_DECLARATION = Pattern.compile("VariableDeclaration \"(.+)\"");
			ENUM_DEFINITION = Pattern.compile("EnumDefinition \"(.+)\"");
			ENUM_VALUE = Pattern.compile("EnumValue \"(.+)\"");
			ELEMENTARY_TYPE_NAME = Pattern.compile("ElementaryTypeName (.+)");
			USER_DEFINED_TYPE_NAME = Pattern.compile("UserDefinedTypeName \"(.+)\"");
			CONSTANT_DETECTOR = Pattern.compile("Source:.+constant.+");
			INHERITANCE_SPECIFIER = Pattern.compile("InheritanceSpecifier");
			LITERAL_DETECTOR = Pattern.compile("Literal\\, token: \\[no token\\] value: (.+)");
		}
	}

	private static class Line {
		private static final int LEVEL_INTENT_LENGTH = 2;
		private String content;
		private int nestingLevel;

		public Line(final String content) {
			this.content = "";
			final Matcher matcher = Patterns.LEVEL_DETECTOR.matcher(content);
			if (matcher.matches()) {
				this.nestingLevel = matcher.group(1).length() / LEVEL_INTENT_LENGTH;
				this.content = matcher.group(2);
			}
		}

		public Matcher matcher(final Pattern pattern) {
			return pattern.matcher(this.content);
		}

		@Override
		public boolean equals(final Object obj) {
			return this.content.equals(obj);
		}

		public String getContent() {
			return this.content;
		}

		public int getNestingLevel() {
			return this.nestingLevel;
		}

		@Override
		public String toString() {
			return "Ast.Line(content=" + this.getContent() + ", nestingLevel=" + this.getNestingLevel() + ")";
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public abstract static class Entry {
		@JsonIgnore
		private int nestingLevel;
		@JsonIgnore
		private Entry parent;
		protected String name;

		Entry(final Entry parent, final String name) {
			this.name = name;
			this.nestingLevel = ((parent == null) ? 0 : (parent.getNestingLevel() + 1));
			this.parent = parent;
		}

		public void validate() {
		}

		public boolean applicable(final Line line) {
			return this.nestingLevel < line.getNestingLevel();
		}

		public abstract void apply(final Line p0);

		protected Root getRoot() {
			final Entry root = (this.parent == null) ? this : this.parent.getRoot();
			return (Root) root;
		}

		public String toJson() {
			try {
				return new ObjectMapper().writeValueAsString((Object) this);
			} catch (JsonProcessingException e) {
				throw new RuntimeException((Throwable) e);
			}
		}

		public int getNestingLevel() {
			return this.nestingLevel;
		}

		public Entry getParent() {
			return this.parent;
		}

		public String getName() {
			return this.name;
		}

		public void setNestingLevel(final int nestingLevel) {
			this.nestingLevel = nestingLevel;
		}

		public void setParent(final Entry parent) {
			this.parent = parent;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Entry)) {
				return false;
			}
			final Entry other = (Entry) o;
			if (!other.canEqual(this)) {
				return false;
			}
			if (this.getNestingLevel() != other.getNestingLevel()) {
				return false;
			}
			final Object this$parent = this.getParent();
			final Object other$parent = other.getParent();
			Label_0078: {
				if (this$parent == null) {
					if (other$parent == null) {
						break Label_0078;
					}
				} else if (this$parent.equals(other$parent)) {
					break Label_0078;
				}
				return false;
			}
			final Object this$name = this.getName();
			final Object other$name = other.getName();
			if (this$name == null) {
				if (other$name == null) {
					return true;
				}
			} else if (this$name.equals(other$name)) {
				return true;
			}
			return false;
		}

		protected boolean canEqual(final Object other) {
			return other instanceof Entry;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + this.getNestingLevel();
			final Object $parent = this.getParent();
			result = result * PRIME + (($parent == null) ? 0 : $parent.hashCode());
			final Object $name = this.getName();
			result = result * PRIME + (($name == null) ? 0 : $name.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "Ast.Entry(nestingLevel=" + this.getNestingLevel() + ", parent=" + this.getParent() + ", name="
					+ this.getName() + ")";
		}

		public Entry() {
		}
	}

	public static class Entries<T extends Entry> extends ArrayList<T> {
		private static final long serialVersionUID = 1L;
		private Pattern pattern;
		private Function<Matcher, T> function;
		private T current;

		public Entries(final Pattern pattern, final Function<Matcher, T> function) {
			this.pattern = pattern;
			this.function = function;
		}

		public void apply(final Line line) {
			T detected = null;
			final Matcher matcher = line.matcher(this.pattern);
			if (matcher.matches()) {
				final T entry = this.function.apply(matcher);
				if (entry.getNestingLevel() == line.getNestingLevel()) {
					detected = entry;
				}
			}
			if (detected != null) {
				this.add(detected);
				this.current = detected;
			} else if (this.current != null) {
				if (this.current.applicable(line)) {
					this.current.apply(line);
				} else {
					this.current.validate();
					this.current = null;
				}
			}
		}

		public Entries() {
		}
	}

	private static class Root extends Entry {
		private Entries<Contract> contracts;
		private Map<String, Set<String>> userDefinedTypes;
		private List<Type.UserDefined> deferredTypeDefinitions;

		public Root() {
			this.contracts = Contract.entries(this);
			this.userDefinedTypes = new HashMap<String, Set<String>>();
			this.deferredTypeDefinitions = new ArrayList<Type.UserDefined>();
			this.setNestingLevel(-1);
		}

		@Override
		public void apply(final Line line) {
			this.contracts.apply(line);
		}

		private Contract findContract(final String name) {
			return this.getOptionalContract(name).get();
		}

		private Optional<Contract> getOptionalContract(final String name) {
			return this.contracts.stream()
					.filter(c -> StringUtils.equals((CharSequence) c.getName(), (CharSequence) name)).findFirst();
		}

		public List<Contract> getContractHierarchy(final String name) {
			return this.findContract(name).hierarchy().stream().map(contractName -> this.findContract(contractName))
					.collect(Collectors.toList());
		}

		private Set<String> getUserDefinedTypesNames(final String type) {
			Set<String> names = this.userDefinedTypes.get(type);
			if (names == null) {
				names = new HashSet<String>();
				this.userDefinedTypes.put(type, names);
			}
			return names;
		}

		public void onUserDefinedDetected(final String type, final String name) {
			this.getUserDefinedTypesNames(type).add(name);
		}

		public boolean isUserDefined(final String type, final String name) {
			return this.getUserDefinedTypesNames(type).contains(name);
		}

		public void addDeferredTypeDefinition(final Type.UserDefined undefinedType) {
			this.deferredTypeDefinitions.add(undefinedType);
		}

		private void resolveTypeDefinitions(final Type.UserDefined type) {
			for (final String typeName : this.userDefinedTypes.keySet()) {
				if (this.isUserDefined(typeName, type.getType())) {
					type.setName(typeName);
				}
			}
		}

		public void resolveDeferredTypeDefinitions() {
			this.deferredTypeDefinitions.stream().forEach(this::resolveTypeDefinitions);
		}
	}

	public static class Inheritance extends Entry {
		Inheritance(final Entry parent, final String name) {
			super(parent, name);
		}

		@JsonCreator
		public static Inheritance fromJson(final String name) {
			return new Inheritance(null, name);
		}

		@Override
		public void apply(final Line line) {
			if (this.applicable(line) && StringUtils.isEmpty((CharSequence) this.name)) {
				final Matcher matcher = line.matcher(Patterns.USER_DEFINED_TYPE_NAME);
				if (matcher.matches()) {
					this.name = matcher.group(1);
				}
			}
		}

		@JsonValue
		@Override
		public String getName() {
			return super.getName();
		}

		public static Entries<Inheritance> entries(final Contract contract) {
			return new Entries<Inheritance>(Patterns.INHERITANCE_SPECIFIER, matcher -> new Inheritance(contract, null));
		}

		@Override
		public String toString() {
			return "Ast.Inheritance()";
		}

		public Inheritance() {
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Inheritance)) {
				return false;
			}
			final Inheritance other = (Inheritance) o;
			return other.canEqual(this) && super.equals(o);
		}

		@Override
		protected boolean canEqual(final Object other) {
			return other instanceof Inheritance;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + super.hashCode();
			return result;
		}
	}

	public static class Contract extends Entry {
		private Entries<Inheritance> inheritances;
		private Entries<Enum> enums;
		private Entries<Structure> structures;
		private Entries<Variable> variables;

		Contract(final Root root, final String name) {
			super(root, name);
			this.inheritances = Inheritance.entries(this);
			this.enums = Enum.entries(this);
			this.structures = Structure.entries(this);
			this.variables = Variable.entries(this);
		}

		public List<String> hierarchy() {
			final List<String> result = new ArrayList<String>();
			for (int i = this.inheritances.size() - 1; i >= 0; --i) {
				final Inheritance inheritance = this.inheritances.get(i);
				final Contract parent = this.getRoot().findContract(inheritance.getName());
				final List<String> hierarchy = parent.hierarchy().stream()
						.filter(parentHierarchy -> !result.contains(parentHierarchy))
						.collect(Collectors.toList());
				result.addAll(0, hierarchy);
			}
			result.add(this.getName());
			return result;
		}

		@Override
		public void apply(final Line line) {
			this.inheritances.apply(line);
			this.enums.apply(line);
			this.structures.apply(line);
			this.variables.apply(line);
		}

		public static Entries<Contract> entries(final Root root) {
			return new Entries<Contract>(Patterns.CONTRACT_DEFINITION, matcher -> {
				String name = matcher.group(1);
				root.onUserDefinedDetected("contract", name);
				return new Contract(root, name);
			});
		}

		public static Contract fromJson(final String json) {
			try {
				return (Contract) new ObjectMapper().readValue(json, (Class) Contract.class);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public Entries<Inheritance> getInheritances() {
			return this.inheritances;
		}

		public Entries<Enum> getEnums() {
			return this.enums;
		}

		public Entries<Structure> getStructures() {
			return this.structures;
		}

		public Entries<Variable> getVariables() {
			return this.variables;
		}

		public void setInheritances(final Entries<Inheritance> inheritances) {
			this.inheritances = inheritances;
		}

		public void setEnums(final Entries<Enum> enums) {
			this.enums = enums;
		}

		public void setStructures(final Entries<Structure> structures) {
			this.structures = structures;
		}

		public void setVariables(final Entries<Variable> variables) {
			this.variables = variables;
		}

		@Override
		public String toString() {
			return "Ast.Contract(inheritances=" + this.getInheritances() + ", enums=" + this.getEnums()
					+ ", structures=" + this.getStructures() + ", variables=" + this.getVariables() + ")";
		}

		public Contract() {
			this.inheritances = Inheritance.entries(this);
			this.enums = Enum.entries(this);
			this.structures = Structure.entries(this);
			this.variables = Variable.entries(this);
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Contract)) {
				return false;
			}
			final Contract other = (Contract) o;
			if (!other.canEqual(this)) {
				return false;
			}
			final Object this$inheritances = this.getInheritances();
			final Object other$inheritances = other.getInheritances();
			Label_0065: {
				if (this$inheritances == null) {
					if (other$inheritances == null) {
						break Label_0065;
					}
				} else if (this$inheritances.equals(other$inheritances)) {
					break Label_0065;
				}
				return false;
			}
			final Object this$enums = this.getEnums();
			final Object other$enums = other.getEnums();
			Label_0102: {
				if (this$enums == null) {
					if (other$enums == null) {
						break Label_0102;
					}
				} else if (this$enums.equals(other$enums)) {
					break Label_0102;
				}
				return false;
			}
			final Object this$structures = this.getStructures();
			final Object other$structures = other.getStructures();
			Label_0139: {
				if (this$structures == null) {
					if (other$structures == null) {
						break Label_0139;
					}
				} else if (this$structures.equals(other$structures)) {
					break Label_0139;
				}
				return false;
			}
			final Object this$variables = this.getVariables();
			final Object other$variables = other.getVariables();
			if (this$variables == null) {
				if (other$variables == null) {
					return true;
				}
			} else if (this$variables.equals(other$variables)) {
				return true;
			}
			return false;
		}

		@Override
		protected boolean canEqual(final Object other) {
			return other instanceof Contract;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $inheritances = this.getInheritances();
			result = result * PRIME + (($inheritances == null) ? 0 : $inheritances.hashCode());
			final Object $enums = this.getEnums();
			result = result * PRIME + (($enums == null) ? 0 : $enums.hashCode());
			final Object $structures = this.getStructures();
			result = result * PRIME + (($structures == null) ? 0 : $structures.hashCode());
			final Object $variables = this.getVariables();
			result = result * PRIME + (($variables == null) ? 0 : $variables.hashCode());
			return result;
		}
	}

	public static class Structure extends Entry {
		private final Entries<Variable> variables;

		Structure(final Contract contract, final String name) {
			super(contract, name);
			this.variables = Variable.entries(this);
		}

		@Override
		public void apply(final Line line) {
			this.variables.apply(line);
		}

		public static Entries<Structure> entries(final Contract contract) {
			return new Entries<Structure>(Patterns.STRUCTURE_DEFINITION, matcher -> {
				String name = matcher.group(1);
				contract.getRoot().onUserDefinedDetected("struct", name);
				return new Structure(contract, name);
			});
		}

		public Entries<Variable> getVariables() {
			return this.variables;
		}

		@Override
		public String toString() {
			return "Ast.Structure(variables=" + this.getVariables() + ")";
		}

		public Structure() {
			this.variables = Variable.entries(this);
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Structure)) {
				return false;
			}
			final Structure other = (Structure) o;
			if (!other.canEqual(this)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			final Object this$variables = this.getVariables();
			final Object other$variables = other.getVariables();
			if (this$variables == null) {
				if (other$variables == null) {
					return true;
				}
			} else if (this$variables.equals(other$variables)) {
				return true;
			}
			return false;
		}

		@Override
		protected boolean canEqual(final Object other) {
			return other instanceof Structure;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + super.hashCode();
			final Object $variables = this.getVariables();
			result = result * PRIME + (($variables == null) ? 0 : $variables.hashCode());
			return result;
		}
	}

	public static class Enum extends Entry {
		private Entries<EnumValue> values;

		public Enum(final Contract contract, final String name) {
			super(contract, name);
			this.values = EnumValue.entries(this);
		}

		@Override
		public void apply(final Line line) {
			this.values.apply(line);
		}

		public static Entries<Enum> entries(final Contract contract) {
			return new Entries<Enum>(Patterns.ENUM_DEFINITION, matcher -> {
				String name = matcher.group(1);
				contract.getRoot().onUserDefinedDetected("enum", name);
				return new Enum(contract, name);
			});
		}

		public Entries<EnumValue> getValues() {
			return this.values;
		}

		public void setValues(final Entries<EnumValue> values) {
			this.values = values;
		}

		@Override
		public String toString() {
			return "Ast.Enum(values=" + this.getValues() + ")";
		}

		public Enum() {
			this.values = EnumValue.entries(this);
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Enum)) {
				return false;
			}
			final Enum other = (Enum) o;
			if (!other.canEqual(this)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			final Object this$values = this.getValues();
			final Object other$values = other.getValues();
			if (this$values == null) {
				if (other$values == null) {
					return true;
				}
			} else if (this$values.equals(other$values)) {
				return true;
			}
			return false;
		}

		@Override
		protected boolean canEqual(final Object other) {
			return other instanceof Enum;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + super.hashCode();
			final Object $values = this.getValues();
			result = result * PRIME + (($values == null) ? 0 : $values.hashCode());
			return result;
		}
	}

	public static class EnumValue extends Entry {
		public EnumValue(final String name) {
			super(null, name);
		}

		public EnumValue(final Enum parent, final String name) {
			super(parent, name);
		}

		@Override
		public void apply(final Line line) {
		}

		@JsonValue
		@Override
		public String getName() {
			return super.getName();
		}

		public static Entries<EnumValue> entries(final Enum anEnum) {
			return new Entries<EnumValue>(Patterns.ENUM_VALUE, matcher -> new EnumValue(anEnum, matcher.group(1)));
		}

		@Override
		public String toString() {
			return "Ast.EnumValue()";
		}

		public EnumValue() {
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof EnumValue)) {
				return false;
			}
			final EnumValue other = (EnumValue) o;
			return other.canEqual(this) && super.equals(o);
		}

		@Override
		protected boolean canEqual(final Object other) {
			return other instanceof EnumValue;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + super.hashCode();
			return result;
		}
	}

	public static class Variable extends Entry {
		private boolean constant;
		private Type type;

		public Variable(final Entry parent, final String name) {
			super(parent, name);
		}

		@Override
		public void apply(final Line line) {
			if (this.type == null) {
				this.type = Type.detectAndCreate(this, line);
				if (this.type == null && !this.constant) {
					this.constant = line.matcher(Patterns.CONSTANT_DETECTOR).matches();
				}
			} else {
				this.type.apply(line);
			}
		}

		public static Entries<Variable> entries(final Entry parent) {
			return new Entries<Variable>(Patterns.VARIABLE_DECLARATION,
					matcher -> new Variable(parent, matcher.group(1)));
		}

		public boolean isConstant() {
			return this.constant;
		}

		public Type getType() {
			return this.type;
		}

		public void setConstant(final boolean constant) {
			this.constant = constant;
		}

		public void setType(final Type type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return "Ast.Variable(constant=" + this.isConstant() + ", type=" + this.getType() + ")";
		}

		public Variable() {
		}

		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Variable)) {
				return false;
			}
			final Variable other = (Variable) o;
			if (!other.canEqual(this)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			if (this.isConstant() != other.isConstant()) {
				return false;
			}
			final Object this$type = this.getType();
			final Object other$type = other.getType();
			if (this$type == null) {
				if (other$type == null) {
					return true;
				}
			} else if (this$type.equals(other$type)) {
				return true;
			}
			return false;
		}

		@Override
		protected boolean canEqual(final Object other) {
			return other instanceof Variable;
		}

		@Override
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + super.hashCode();
			result = result * PRIME + (this.isConstant() ? 79 : 97);
			final Object $type = this.getType();
			result = result * PRIME + (($type == null) ? 0 : $type.hashCode());
			return result;
		}
	}

	public abstract static class Type extends Entry {
		Type(final Entry parent, final String name) {
			super(parent, name);
		}

		@Override
		public void apply(final Line line) {
		}

		@JsonIgnore
		public boolean is(final Predicate<String> predicate) {
			return predicate.test(this.getName());
		}

		@JsonIgnore
		public boolean is(final String typeName) {
			return this.is(name -> StringUtils.equals((CharSequence) typeName, (CharSequence) name));
		}

		@JsonIgnore
		public boolean isElementary() {
			return false;
		}

		@JsonIgnore
		public boolean isContainer() {
			return false;
		}

		@JsonIgnore
		public boolean isUserDefined() {
			return false;
		}

		@JsonIgnore
		public boolean isMapping() {
			return this.is("mapping");
		}

		@JsonIgnore
		public boolean isArray() {
			return this.is("array");
		}

		@JsonIgnore
		public boolean isStruct() {
			return this.is("struct");
		}

		@JsonIgnore
		public boolean isStructArray() {
			return this.isArray() && this.as(Array.class).getElementType().isStruct();
		}

		@JsonIgnore
		public boolean isStaticArray() {
			return this.isArray() && this.asArray().isStatic();
		}

		@JsonIgnore
		public boolean isEnum() {
			return this.is("enum");
		}

		@JsonIgnore
		public boolean isContract() {
			return this.is("contract");
		}

		protected <T> T as(final Class<T> castClass) {
			return (T) this;
		}

		public Array asArray() {
			return this.as(Array.class);
		}

		public Mapping asMapping() {
			return this.as(Mapping.class);
		}

		public Struct asStruct() {
			return this.as(Struct.class);
		}

		public Enum asEnum() {
			return this.as(Enum.class);
		}

		public Elementary asElementary() {
			return this.as(Elementary.class);
		}

		public String formatName() {
			return this.getName();
		}

		public static Type detectAndCreate(final Entry parent, final Line line) {
			Matcher matcher = line.matcher(Patterns.ELEMENTARY_TYPE_NAME);
			if (matcher.matches()) {
				final String name = matcher.group(1);
				return new Elementary(parent, name);
			}
			matcher = line.matcher(Patterns.USER_DEFINED_TYPE_NAME);
			if (matcher.matches()) {
				final String name = matcher.group(1);
				final Root root = parent.getRoot();
				if (root.isUserDefined("struct", name)) {
					return new Struct(parent, name);
				}
				if (root.isUserDefined("enum", name)) {
					return new Enum(parent, name);
				}
				if (root.isUserDefined("contract", name)) {
					return new Contract(parent, name);
				}
				final UserDefined undefined = new UserDefined(parent, "unknown", name);
				root.addDeferredTypeDefinition(undefined);
				return undefined;
			} else {
				if (line.equals("Mapping")) {
					return new Mapping(parent);
				}
				if (line.equals("ArrayTypeName")) {
					return new Array(parent);
				}
				return null;
			}
		}

		@JsonCreator
		public static Type fromJson(final Object json) {
			return (json instanceof String) ? fromJson((String) json) : fromJson((Map<String, Object>) json);
		}

		@JsonCreator
		public static Type fromJson(final String typeName) {
			return new Elementary(null, typeName);
		}

		@JsonCreator
		public static Type fromJson(final Map<String, Object> typeProps) {
			final String name = typeProps.get("name").toString();
			if ("array".equals(name)) {
				final Array array = new Array(null);
				array.setElementType(fromJson(typeProps.get("elementType")));
				array.setSize(NumberUtils.toInt(typeProps.computeIfAbsent("size", s -> 0).toString()));
				return array;
			}
			if ("mapping".equals(name)) {
				final Mapping mapping = new Mapping(null);
				mapping.setKeyType(fromJson(typeProps.get("keyType")));
				mapping.setValueType(fromJson(typeProps.get("valueType")));
				return mapping;
			}
			if ("struct".equals(name)) {
				return new Struct(null, typeProps.get("type").toString());
			}
			if ("enum".equals(name)) {
				return new Enum(null, typeProps.get("type").toString());
			}
			if ("contract".equals(name)) {
				return new Contract(null, typeProps.get("type").toString());
			}
			return null;
		}

		public Type() {
		}

		protected static class Names {
			public static final String MAPPING = "mapping";
			public static final String ARRAY = "array";
			public static final String STRUCT = "struct";
			public static final String ENUM = "enum";
			public static final String CONTRACT = "contract";
			public static final String UNKNOWN = "unknown";
		}

		public static class Elementary extends Type {
			public Elementary(final Entry parent, final String name) {
				super(parent, name);
			}

			@JsonValue
			@Override
			public String getName() {
				return super.getName();
			}

			@Override
			public boolean isElementary() {
				return true;
			}

			@JsonIgnore
			public boolean isString() {
				return this.is("string");
			}

			@JsonIgnore
			public boolean isBool() {
				return this.is("bool");
			}

			@JsonIgnore
			public boolean isAddress() {
				return this.is("address");
			}

			@JsonIgnore
			public boolean isNumber() {
				return this.is(name1 -> StringUtils.contains((CharSequence) name1, (CharSequence) "int"));
			}

			public Elementary() {
			}
		}

		public abstract static class Container extends Type {
			public Container(final Entry parent, final String name) {
				super(parent, name);
			}

			@JsonIgnore
			@Override
			public boolean isContainer() {
				return true;
			}

			@Override
			public String toString() {
				return "Ast.Type.Container()";
			}

			public Container() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof Container)) {
					return false;
				}
				final Container other = (Container) o;
				return other.canEqual(this) && super.equals(o);
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof Container;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				return result;
			}
		}

		public static class Array extends Container {
			private Type elementType;
			private Integer size;

			Array(final Entry parent) {
				super(parent, "array");
			}

			@JsonIgnore
			public boolean isStatic() {
				return this.size != null && this.size > 0;
			}

			@Override
			public void apply(final Line line) {
				if (this.elementType == null) {
					this.elementType = Type.detectAndCreate(this, line);
				} else if (this.elementType.applicable(line)) {
					this.elementType.apply(line);
				} else if (Objects.isNull(this.size)) {
					final Matcher matcher = line.matcher(Patterns.LITERAL_DETECTOR);
					if (matcher.matches()) {
						final String size = matcher.group(1);
						this.size = NumberUtils.toInt(size);
					}
				}
			}

			@Override
			public String formatName() {
				return String.format("%s[%s]", this.elementType.formatName(), this.isStatic() ? this.size : "");
			}

			public Type getElementType() {
				return this.elementType;
			}

			public Integer getSize() {
				return this.size;
			}

			public void setElementType(final Type elementType) {
				this.elementType = elementType;
			}

			public void setSize(final Integer size) {
				this.size = size;
			}

			@Override
			public String toString() {
				return "Ast.Type.Array(elementType=" + this.getElementType() + ", size=" + this.getSize() + ")";
			}

			public Array() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof Array)) {
					return false;
				}
				final Array other = (Array) o;
				if (!other.canEqual(this)) {
					return false;
				}
				if (!super.equals(o)) {
					return false;
				}
				final Object this$elementType = this.getElementType();
				final Object other$elementType = other.getElementType();
				Label_0075: {
					if (this$elementType == null) {
						if (other$elementType == null) {
							break Label_0075;
						}
					} else if (this$elementType.equals(other$elementType)) {
						break Label_0075;
					}
					return false;
				}
				final Object this$size = this.getSize();
				final Object other$size = other.getSize();
				if (this$size == null) {
					if (other$size == null) {
						return true;
					}
				} else if (this$size.equals(other$size)) {
					return true;
				}
				return false;
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof Array;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				final Object $elementType = this.getElementType();
				result = result * PRIME + (($elementType == null) ? 0 : $elementType.hashCode());
				final Object $size = this.getSize();
				result = result * PRIME + (($size == null) ? 0 : $size.hashCode());
				return result;
			}
		}

		public static class Mapping extends Container {
			private Type keyType;
			private Type valueType;

			Mapping(final Entry parent) {
				super(parent, "mapping");
			}

			@Override
			public void apply(final Line line) {
				if (this.keyType == null) {
					this.keyType = Type.detectAndCreate(this, line);
				} else if (this.valueType == null && this.keyType.applicable(line)) {
					this.keyType.apply(line);
				} else if (this.valueType == null) {
					this.valueType = Type.detectAndCreate(this, line);
				} else if (this.valueType.applicable(line)) {
					this.valueType.apply(line);
				}
			}

			@Override
			public String formatName() {
				return String.format("mapping(%s=>%s)", this.keyType.formatName(), this.valueType.formatName());
			}

			public Type getKeyType() {
				return this.keyType;
			}

			public Type getValueType() {
				return this.valueType;
			}

			public void setKeyType(final Type keyType) {
				this.keyType = keyType;
			}

			public void setValueType(final Type valueType) {
				this.valueType = valueType;
			}

			@Override
			public String toString() {
				return "Ast.Type.Mapping(keyType=" + this.getKeyType() + ", valueType=" + this.getValueType() + ")";
			}

			public Mapping() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof Mapping)) {
					return false;
				}
				final Mapping other = (Mapping) o;
				if (!other.canEqual(this)) {
					return false;
				}
				if (!super.equals(o)) {
					return false;
				}
				final Object this$keyType = this.getKeyType();
				final Object other$keyType = other.getKeyType();
				Label_0075: {
					if (this$keyType == null) {
						if (other$keyType == null) {
							break Label_0075;
						}
					} else if (this$keyType.equals(other$keyType)) {
						break Label_0075;
					}
					return false;
				}
				final Object this$valueType = this.getValueType();
				final Object other$valueType = other.getValueType();
				if (this$valueType == null) {
					if (other$valueType == null) {
						return true;
					}
				} else if (this$valueType.equals(other$valueType)) {
					return true;
				}
				return false;
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof Mapping;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				final Object $keyType = this.getKeyType();
				result = result * PRIME + (($keyType == null) ? 0 : $keyType.hashCode());
				final Object $valueType = this.getValueType();
				result = result * PRIME + (($valueType == null) ? 0 : $valueType.hashCode());
				return result;
			}
		}

		public static class UserDefined extends Type {
			private String type;

			UserDefined(final Entry parent, final String name, final String type) {
				super(parent, name);
				this.type = type;
			}

			@Override
			public boolean isUserDefined() {
				return true;
			}

			@Override
			public String formatName() {
				return this.type;
			}

			public String getType() {
				return this.type;
			}

			public void setType(final String type) {
				this.type = type;
			}

			@Override
			public String toString() {
				return "Ast.Type.UserDefined(type=" + this.getType() + ")";
			}

			public UserDefined() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof UserDefined)) {
					return false;
				}
				final UserDefined other = (UserDefined) o;
				if (!other.canEqual(this)) {
					return false;
				}
				if (!super.equals(o)) {
					return false;
				}
				final Object this$type = this.getType();
				final Object other$type = other.getType();
				if (this$type == null) {
					if (other$type == null) {
						return true;
					}
				} else if (this$type.equals(other$type)) {
					return true;
				}
				return false;
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof UserDefined;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				final Object $type = this.getType();
				result = result * PRIME + (($type == null) ? 0 : $type.hashCode());
				return result;
			}
		}

		public static class Struct extends UserDefined {
			Struct(final Entry parent, final String type) {
				super(parent, "struct", type);
			}

			@Override
			public String toString() {
				return "Ast.Type.Struct()";
			}

			public Struct() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof Struct)) {
					return false;
				}
				final Struct other = (Struct) o;
				return other.canEqual(this) && super.equals(o);
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof Struct;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				return result;
			}
		}

		public static class Enum extends UserDefined {
			Enum(final Entry parent, final String type) {
				super(parent, "enum", type);
			}

			@Override
			public String toString() {
				return "Ast.Type.Enum()";
			}

			public Enum() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof Enum)) {
					return false;
				}
				final Enum other = (Enum) o;
				return other.canEqual(this) && super.equals(o);
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof Enum;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				return result;
			}
		}

		public static class Contract extends UserDefined {
			Contract(final Entry parent, final String type) {
				super(parent, "contract", type);
			}

			@Override
			public String toString() {
				return "Ast.Type.Contract()";
			}

			public Contract() {
			}

			@Override
			public boolean equals(final Object o) {
				if (o == this) {
					return true;
				}
				if (!(o instanceof Contract)) {
					return false;
				}
				final Contract other = (Contract) o;
				return other.canEqual(this) && super.equals(o);
			}

			@Override
			protected boolean canEqual(final Object other) {
				return other instanceof Contract;
			}

			@Override
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				result = result * PRIME + super.hashCode();
				return result;
			}
		}
	}
}
