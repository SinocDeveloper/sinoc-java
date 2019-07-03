package org.sinoc.shell.service.contracts;

import org.sinoc.shell.util.exception.ContractException;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.subtract;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringsBetween;

public class Source {

	private String name;
	private String content;
	private List<String> dependencies;

	public Source(String name, String content) {
		this.name = name;
		this.content = content;
	}

	public List<String> getDependencies() {
		if (dependencies == null) {
			String withoutCommentedImports = content.replaceAll("//\\s*import\\s", "");
			String[] imports = substringsBetween(withoutCommentedImports, "import \"", "\";");
			dependencies = isEmpty(imports) ? emptyList() : asList(imports);
		}
		return dependencies;
	}

	public String toPlain(Map<String, Source> dependencies) {
		String result = content;

		for (String dependency : getDependencies()) {
			Source source = dependencies.get(dependency);
			if (isNull(source)) {
				throw ContractException.assembleError("dependency '%s' not found", dependency);
			}
			result = result.replace(format("import \"%s\";", dependency), source.toPlain(dependencies));
		}

		return result;
	}

	public static List<String> toPlain(MultipartFile[] files) {
		Map<String, Source> srcByName = Arrays.stream(files).map(file -> {
			try {
				return new Source(file.getOriginalFilename(), new String(file.getBytes(), "UTF-8"));
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}).collect(toMap(Source::getName, identity()));

		return tp(srcByName);
	}

	private static List<String> tp(Map<String, Source> srcByName) {
		Set<String> allDependencies = srcByName.values().stream().flatMap(src -> src.getDependencies().stream())
				.collect(toSet());

		return subtract(srcByName.keySet(), allDependencies).stream()
				.map(target -> srcByName.get(target).toPlain(srcByName)).collect(toList());
	}

	@Override
	public String toString() {
		return this.name;
	};

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = (result * PRIME) + (this.name == null ? 0 : this.name.hashCode());
		result = (result * PRIME) + (this.content==null ? 0: this.content.hashCode());
		result = (result * PRIME) + (this.dependencies==null? 0: this.dependencies.hashCode());
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}
}
