package com.zarbosoft.bonestruct.modules;

import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Common.Mutable;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration(name = "lua_actions")
public class LuaActions extends Module {
	@Configuration()
	public Map<String, LuaValue> actions;

	@Override
	public State initialize(final Context context) {
		return new ModuleState(context);
	}

	private class ModuleState extends State {
		ModuleState(final Context context) {
			context.addActions(this, actions.entrySet().stream().map(entry -> {
				return new ActionAct(entry);
			}).collect(Collectors.toList()));
		}

		@Override
		public void destroy(final Context context) {
			context.removeActions(this);
		}
	}

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "lua actions module";
		}
	}

	@Action.StaticID(id = "%s (%s = lua action id)")
	private static class ActionAct extends ActionBase {
		private final Map.Entry<String, LuaValue> entry;

		public ActionAct(final Map.Entry<String, LuaValue> entry) {
			this.entry = entry;
		}

		@Override
		public boolean run(final Context context) {
			final LuaTable luaContext = new LuaTable();
			final Mutable<Context> context2 = new Mutable<>(context);
			luaContext.set(LuaValue.valueOf("act"), new OneArgFunction() {
				@Override
				public LuaValue call(final LuaValue luaValue) {
					final String name = luaValue.tojstring();
					return LuaValue.valueOf(context2.value
							.actions()
							.filter(action -> name.equals(action.id()))
							.findAny()
							.map(action -> action.run(context2.value))
							.orElse(false));
				}
			});
			final boolean out = entry.getValue().call(luaContext).toboolean();
			context2.value = null;
			return out;
		}

		@Override
		public String id() {
			return entry.getKey();
		}
	}
}
