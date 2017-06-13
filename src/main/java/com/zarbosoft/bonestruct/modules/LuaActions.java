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

@Configuration(name = "lua_actions", description = "Use Lua functions as bindable actions.")
public class LuaActions extends Module {
	@Configuration(description = "Actions as functions mapped to names.  Each function takes a single `context` " +
			"argument and returns a boolean, true if the action modified the application state.  `context` " +
			"is an object with the following functions: `act`.  `act` takes a string, the name of another action, and " +
			"runs it, and returns true if the action modified the application state.")
	public Map<String, LuaValue> actions;

	@Override
	public State initialize(final Context context) {
		context.addActions(this, actions.entrySet().stream().map(entry -> {
			return new Action() {
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
									.filter(action -> name.equals(action.getName()))
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
				public String getName() {
					return entry.getKey();
				}
			};
		}).collect(Collectors.toList()));
		return new State() {
			@Override
			public void destroy(final Context context) {
				context.removeActions(LuaActions.this);
			}
		};
	}
}
