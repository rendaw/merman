local _utils = require 'utils'
local module = {}
function module.create()
    return {
        modules = {
            lua_actions {
                super_next = function(context)
                    if context.act('next_element') then
                        return true
                    else
                        return context.act('next')
                    end
                end,
                super_previous = function(context)
                    if context.act('previous_element') then
                        return true
                    else
                        return context.act('previous')
                    end
                end,
            },
            hotkeys {
                rules = {
                    {
                        hotkeys = {
                            exit = { key 'escape' },
                            debug = { key { key = 'd', modifiers = { 'control' } } },
                            enter = { key 'enter' },
                            undo = { key { key = 'z', modifiers = { 'control' } } },
                            redo = { key { key = 'z', modifiers = { 'control', 'shift' } } },
                            choose = { key { key = 'space', modifiers = { 'control' } } },
                            choose_0 = { key { key = '0', modifiers = { 'control' } } },
                            choose_1 = { key { key = '1', modifiers = { 'control' } } },
                            choose_2 = { key { key = '2', modifiers = { 'control' } } },
                            choose_3 = { key { key = '3', modifiers = { 'control' } } },
                            choose_4 = { key { key = '4', modifiers = { 'control' } } },
                            choose_5 = { key { key = '5', modifiers = { 'control' } } },
                            choose_6 = { key { key = '6', modifiers = { 'control' } } },
                            choose_7 = { key { key = '7', modifiers = { 'control' } } },
                            choose_8 = { key { key = '8', modifiers = { 'control' } } },
                            choose_9 = { key { key = '9', modifiers = { 'control' } } },
                            delete = { key 'x' },
                            delete_next = { key 'delete' },
                            delete_previous = { key 'backspace' },
                            super_next = { key 'right' },
                            super_previous = { key 'left' },
                            next_word = { key { key = 'right', modifiers = { 'control' } }, },
                            previous_word = { key { key = 'left', modifiers = { 'control' } }, },
                            next_line = { key 'down' },
                            previous_line = { key 'up' },
                            line_begin = { key 'home' },
                            line_end = { key 'end' },
                            gather_next = { key { key = 'right', modifiers = { 'shift' } }, },
                            gather_previous = { key { key = 'left', modifiers = { 'shift' } }, },
                            gather_next_line = { key { key = 'down', modifiers = { 'shift' } }, },
                            gather_previous_line = { key { key = 'up', modifiers = { 'shift' } }, },
                            gather_line_start = { key { key = 'home', modifiers = { 'shift' } }, },
                            gather_line_end = { key { key = 'end', modifiers = { 'shift' } }, },
                            copy = { key { key = 'c', modifiers = { 'control' } } },
                            cut = { key { key = 'x', modifiers = { 'control' } } },
                            paste = { key { key = 'v', modifiers = { 'control' } } },
                            insert_before = { key 'b' },
                            insert_after = { key 'a' },
                            prefix = { key { key = 'i', modifiers = { 'control' } } },
                            suffix = { key { key = 'a', modifiers = { 'control' } } },
                            next_choice = { key { key = 'down', modifiers = { 'control' } } },
                            previous_choice = { key { key = 'up', modifiers = { 'control' } } },
                            click_hovered = { key 'mouse1' },
                            scroll_previous = { key 'mouse_scroll_up' },
                            scroll_next = { key 'mouse_scroll_down' },
                        },
                    },
                    {
                        with = { part 'atom' },
                        hotkeys = {
                            next = { key 'right' },
                            previous = { key 'left' },
                        },
                    }
                }
            },
        },
        apply = function(self, syntax)
            _utils.append_multiple(syntax['modules'], self.modules)
        end,
    }
end

return module
