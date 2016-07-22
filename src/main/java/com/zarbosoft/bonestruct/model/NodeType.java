package com.zarbosoft.bonestruct.model;

import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.bonestruct.model.back.BackPart;
import com.zarbosoft.bonestruct.model.front.FrontPart;
import com.zarbosoft.bonestruct.model.middle.*;
import com.zarbosoft.bonestruct.visual.VisualNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Sequence;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

import java.util.*;

@Luxem.Configuration
public class NodeType {
	@Luxem.Configuration
	public String id;

	@Luxem.Configuration
	public String name;

	@Luxem.Configuration
	public List<FrontPart> front;

	@Luxem.Configuration
	public List<BackPart> back;

	@Luxem.Configuration
	public List<DataElement> middle;

	public com.zarbosoft.pidgoon.internal.Node buildLoadRule() {
		final Sequence seq = new Sequence();
		back.forEach(p -> seq.add(p.buildLoadRule()));
		return new BakedOperator(seq, store -> {
			final Map<String, Object> data = new HashMap<>();
			final Pair<String, Object> pair = store.stackTop();
			store = (Store) store.popStack();
			data.put(pair.first, pair.second);
			final Node node = new Node();
			node.data = data;
			node.type = this;
			return store.pushStack(node);
		});
	}

	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		final Set<String> middleKeys = new HashSet<>();
		{
			middle.forEach(p -> {
				if (middleKeys.contains(p.key))
					throw new InvalidSyntax(String.format("Multiple data parts with key [%s] in %s.", p.key, this));
				middleKeys.add(p.key);
				p.finish(singleNodes, arrayNodes);
			});
		}
		{
			final Set<String> middleUsedBack = new HashSet<>();
			back.forEach(p -> p.finish(this, middleUsedBack));
			final Set<String> missing = Sets.difference(middleKeys, middleUsedBack);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Data elements %s in %s are unused by back parts.",
						this,
						missing
				));
		}
		{
			final Set<String> middleUsedFront = new HashSet<>();
			front.forEach(p -> p.finish(this, middleUsedFront));
			final Set<String> missing = Sets.difference(middleKeys, middleUsedFront);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Data elements %s in %s are unused by front parts.",
						this,
						missing
				));
		}
	}

	private <D extends DataElement> D getData(final Class<? extends DataElement> type, final String key) {
		final Optional<DataElement> prefound = middle.stream().filter(e -> e.key.equals(key)).findAny();
		final DataElement found;
		if (!prefound.isPresent()) {
			throw new InvalidSyntax(String.format("No data field named [%s] in %s", key, this));
			/*
			found = Helper.uncheck(type::newInstance);
			found.key = key;
			middle.add(found);
			*/
		} else {
			found = prefound.get();
			if (!type.isAssignableFrom(found.getClass()))
				throw new InvalidSyntax(String.format("Conflicting types for data field %s in %s: %s, %s",
						key,
						this,
						found.getClass(),
						type
				));
		}
		return (D) found;
	}

	public DataPrimitive getDataPrimitive(final String key) {
		return getData(DataPrimitive.class, key);
	}

	public DataNode getDataNode(final String key) {
		return getData(DataNode.class, key);
	}

	public DataArray getDataArray(final String key) {
		return getData(DataArray.class, key);
	}

	public DataRecord getDataRecord(final String key) {
		return getData(DataRecord.class, key);
	}

	public VisualNode createVisual(final Map<String, Object> data) {
		class Visual implements VisualNode {
			List<VisualNode> children = new ArrayList<>();
			Point2D end = new Point2D(0, 0);
			Pane pane = new Pane();

			public void add(final VisualNode node) {
				this.children.add(node);
				pane.getChildren().add(node.visual());
			}

			@Override
			public Point2D end() {
				return end;
			}

			@Override
			public javafx.scene.Node visual() {
				return pane;
			}

			@Override
			public void offset(final Point2D offset) {
				pane.setTranslateX(offset.getX());
				pane.setTranslateY(offset.getY());
			}

			@Override
			public Iterator<VisualNode> children() {
				return children.iterator();
			}

			@Override
			public void layoutInitial() {
				end = Point2D.ZERO;
				for (final VisualNode child : children) {
					child.layoutInitial();
					child.offset(end);
					end = end.add(child.end());
				}
			}
		}
		final Visual out = new Visual();
		for (final FrontPart part : front) {
			out.add(part.createVisual(data));
		}
		return out;
	}

	@Override
	public String toString() {
		return id;
	}
}
