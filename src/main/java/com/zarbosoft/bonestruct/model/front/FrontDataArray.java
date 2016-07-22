package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataArray;
import com.zarbosoft.bonestruct.visual.GroupVisualNode;
import com.zarbosoft.bonestruct.visual.VisualNode;
import com.zarbosoft.luxemj.Luxem;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "array")
public class FrontDataArray implements FrontPart {

	@Luxem.Configuration
	public String key;
	@Luxem.Configuration
	public List<FrontConstantPart> prefix;
	@Luxem.Configuration
	public List<FrontConstantPart> suffix;
	@Luxem.Configuration
	public List<FrontConstantPart> separator;
	private DataArray dataType;

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		dataType = nodeType.getDataArray(key);
	}

	@Override
	public VisualNode createVisual(final Map<String, Object> data) {
		class ArrayElementVisual extends GroupVisualNode {
			public ArrayElementVisual(final boolean first, final Node node) {
				for (final FrontConstantPart fix : prefix)
					add(fix.createVisual());
				add(node.createVisual());
				for (final FrontConstantPart fix : suffix)
					add(fix.createVisual());
				if (first) {
					for (final FrontConstantPart fix : separator)
						add(fix.createVisual());
				}
			}
		}
		class ArrayVisual extends GroupVisualNode {
		}
		final ArrayVisual out = new ArrayVisual();
		boolean first = true;
		for (final Node node : dataType.get(data)) {
			out.add(new ArrayElementVisual(first, node));
			first = false;
		}
		return out;
	}
}
