/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Name("Node")
@Description({
	"Returns a node inside a config (or another section-node).",
})
@Examples({})
@Since("INSERT VERSION")
public class ExprNode extends PropertyExpression<Node, Node> {

	static {
		Skript.registerExpression(ExprNode.class, Node.class, ExpressionType.PROPERTY,
				"[the] node %string% (of|in) %node%",
				"%node%'[s] node %string%",
				"[the] nodes (of|in) %nodes%",
				"%node%'[s] nodes"
		);
	}

	private boolean isPath;
	private Expression<String> pathExpression;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
		this.isPath = pattern < 2;
		switch (pattern) {
			case 0:
				this.pathExpression = (Expression<String>) expressions[0];
				this.setExpr((Expression<? extends Node>) expressions[1]);
				break;
			case 1:
				this.pathExpression = (Expression<String>) expressions[1];
				this.setExpr((Expression<? extends Node>) expressions[0]);
				break;
			default:
				this.setExpr((Expression<? extends Node>) expressions[0]);
		}
		return true;
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null; // todo editable configs in future?
	}

	@Override
	public Class<? extends Node> getReturnType() {
		return Node.class;
	}

	@Override
	protected Node[] get(Event event, Node[] source) {
		if (source.length == 0)
			return CollectionUtils.array();
		if (isPath) {
			String path = pathExpression.getSingle(event);
			Node node = source[0];
			if (node != null && (path == null || path.isBlank()))
				return CollectionUtils.array(node);
			else if (path == null || node == null)
				return CollectionUtils.array();
			node = node.getNodeAt(path);
			if (node == null)
				return CollectionUtils.array();
			return CollectionUtils.array(node);
		} else {
			Set<Node> nodes = new LinkedHashSet<>();
			for (Node node : source) {
				for (Node inner : node)
					nodes.add(inner);
			}
			return nodes.toArray(new Node[0]);
		}
	}

	@Override
	public @Nullable Iterator<? extends Node> iterator(Event event) {
		if (isPath)
			return super.iterator(event);
		Node single = this.getExpr().getSingle(event);
		if (single instanceof SectionNode)
			return ((SectionNode) single).iterator();
		return null;
	}

	@Override
	public boolean isSingle() {
		return isPath;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (isPath)
			return "node " + pathExpression.toString(event, debug) + " of " + this.getExpr().toString(event, debug);
		return "the nodes of " + this.getExpr().toString(event, debug);
	}

}