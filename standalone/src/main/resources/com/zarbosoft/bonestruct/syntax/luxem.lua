_append = function(table, value)
    table[#table + 1] = value
end

_append_multiple = function(table, values)
    for i, v in pairs(values) do
        _append(table, v)
    end
end

_indicators = indicators {
    indicators = {
        {
            id = 'window',
            symbol = text 'w',
            tags = { global 'window' },
        },
    },
}

_modal_keys = function(config)
    _append(config['modules'], modes {
        states = {
            'notyping',
            'typing',
        },
    })
    _append(config['modules'], modes {
        states = {
            'nogather',
            'gather',
            'reduce',
        }
    })
    _append(config['modules'], hotkeys {
        rules = {
            {
                with = { global 'mode_notyping' },
                hotkeys = {
                    exit = { key 'left', key 'h', key 'escape' },
                    enter = { key 'right', key 'l', key 'enter', key 'space' },
                    undo = { key 'u' },
                    redo = { key { key = 'u', modifiers = { 'shift' } } },
                    next = { key 'down', key 'j' },
                    previous = { key 'up', key 'k' },
                    gather_next = {
                        key { key = 'down', modifiers = { 'shift' } },
                        key { key = 'j', modifiers = { 'shift' } },
                    },
                    gather_previous = {
                        key { key = 'up', modifiers = { 'shift' } },
                        key { key = 'k', modifiers = { 'shift' } },
                    },
                    mode_gather = { key 'v' },
                    mode_reduce = { key 'r' },
                    cut = { key 'x' },
                    paste_before = { key { key = 'p', modifiers = { 'shift' } } },
                    paste_after = { key 'p' },
                    insert_before = { key 'i' },
                    insert_after = { key 'a' },
                    prefix = { key { key = 'i', modifiers = { 'shift' } } },
                    suffix = { key { key = 'a', modifiers = { 'shift' } } },
                },
                free_typing = false,
            },
            {
                with = { global 'mode_gather' },
                hotkeys = {
                    mode_nogather = { key 'v' },
                    next = {},
                    previous = {},
                    gather_next = { key 'down', key 'j' },
                    gather_previous = { key 'up', key 'k' },
                }
            },
            {
                with = { global 'mode_reduce' },
                hotkeys = {
                    mode_nogather = { key 'r' },
                    next = {},
                    previous = {},
                    reduce_next = { key 'down', key 'j' },
                    reduce_previous = { key 'up', key 'k' },
                }
            },
            {
                with = { part 'primitive' },
                hotkeys = {
                    line_start = { key 'home' },
                    line_end = { key 'end' },
                },
            },
            {
                with = { global 'mode_notyping', part 'primitive' },
                hotkeys = {
                    mode_typing = { key 'enter', key 'i' },
                    next = { key 'right', key 'l' },
                    previous = { key 'left', key 'h' },
                    next_line = { key 'down', key 'j' },
                    previous_line = { key 'up', key 'k' },
                    gather_next = {
                        key { key = 'right', modifiers = { 'shift' } },
                        key { key = 'l', modifiers = { 'shift' } },
                    },
                    gather_previous = {
                        key { key = 'left', modifiers = { 'shift' } },
                        key { key = 'h', modifiers = { 'shift' } },
                    },
                    gather_next_line = {
                        key { key = 'down', modifiers = { 'shift' } },
                        key { key = 'j', modifiers = { 'shift' } },
                    },
                    gather_previous_line = {
                        key { key = 'up', modifiers = { 'shift' } },
                        key { key = 'k', modifiers = { 'shift' } },
                    },
                }
            },
            {
                with = { global 'mode_typing', part 'primitive' },
                hotkeys = {
                    mode_notyping = { key 'escape' },
                    delete_next = { key 'delete' },
                    delete_previous = { key 'backspace' },
                    next = { key 'right' },
                    previous = { key 'left' },
                    next_line = { key 'down' },
                    previous_line = { key 'up' },
                    gather_next = {
                        key { key = 'right', modifiers = { 'shift' } },
                    },
                    gather_previous = {
                        key { key = 'left', modifiers = { 'shift' } },
                    },
                    gather_next_line = {
                        key { key = 'down', modifiers = { 'shift' } },
                    },
                    gather_previous_line = {
                        key { key = 'up', modifiers = { 'shift' } },
                    },
                }
            },
        },
    })
    _append_multiple(indicators.indicators, {
        {
            id = 'typing',
            symbol = text 't',
            tags = { global 'mode_typing' },
        },
        {
            id = 'notyping',
            symbol = text 'n',
            tags = { global 'mode_notyping' },
        },
        {
            id = 'gather',
            symbol = text 'g',
            tags = { global 'mode_gather' },
        },
        {
            id = 'reduce',
            symbol = text 'g',
            tags = { global 'mode_reduce' },
        },
    })
    return config
end

_lessmodal_keys = function(config)
    _append(config['modules'], hotkeys {
        rules = {
            {
                hotkeys = {
                    exit = { key 'escape' },
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
    })
    return config
end


return _lessmodal_keys {
    name = 'luxem',
    --[[pad = {
        converse_start = 15,
        converse_end = 5,
        transverse_start = 60,
        transverse_end = 60,
    },]]
    banner_pad = {
        converse_start = 15,
    },
    detail_pad = {
        converse_start = 15,
    },
    background = rgb { r = 74 / 255, g = 60 / 255, b = 89 / 255 },
    animate_course_placement = true,
    mouse_peek = true,
    styles = {
        {
            color = rgb { r = 163 / 255, g = 164 / 255, b = 232 / 255 },
            font_size = 16,
        },
        {
            with = { part 'details' },
            font_size = 12,
        },
        {
            with = { part 'banner' },
            font_size = 12,
        },
        {
            with = { part 'primitive' },
            color = rgb { r = 55 / 255, g = 83 / 255, b = 232 / 255 },
        },
        {
            with = { part 'gap' },
            color = rgb { r = 255 / 255, g = 249 / 255, b = 106 / 255 },
        },
        {
            with = { part 'hover' },
            obbox = {
                line_color = rgb { r = 112 / 255, g = 80 / 255, b = 145 / 255 },
            },
        },
        {
            with = { part 'selection' },
            obbox = {
                line_color = rgb { r = 101 / 255, g = 82 / 255, b = 122 / 255 },
            },
        },
        {
            with = { state 'compact' },
            color = rgb { r = 255 / 255, g = 0 / 255, b = 0 / 255 },
        },
        {
            with = { free 'split', state 'compact' },
            split = true,
        },
        {
            with = { free 'base', state 'compact' },
            align = 'base',
        },
        {
            with = { free 'indent', state 'compact' },
            align = 'indent',
        },
        {
            with = { free 'record_table' },
            without = { state 'compact' },
            align = 'record_table',
        },
    },
    groups = {
        value = {
            'luxem_object',
            'luxem_array',
            'luxem_primitive',
        },
    },
    root = 'value',
    root_alignments = {
        indent = absolute { offset = 16 },
    },
    root_front = {
        prefix = { { type = space {}, tags = { 'split' } } },
        separator = { { type = text ',' } },
    },
    types = {
        {
            id = 'luxem_object',
            name = 'Luxem Object',
            back = { data_record 'data' },
            middle = {
                data = record {
                    type = 'luxem_object_element',
                }
            },
            alignments = {
                base = relative { base = 'indent', offset = 0 },
                indent = relative { base = 'indent', offset = 16 },
                record_table = concensus {},
            },
            front = {
                symbol { type = text '{' },
                array {
                    prefix = { { type = space {}, tags = { 'indent', 'split' } } },
                    middle = 'data',
                    separator = { text ', ' },
                },
                symbol { type = text '}', tags = { 'split', 'base' }, },
            },
            auto_choose_ambiguity = 999,
            precedence = 0,
        },
        {
            id = 'luxem_object_element',
            name = 'Luxem Object Element',
            back = { data_key 'key', data_atom 'value' },
            middle = {
                key = key {},
                value = atom 'value',
            },
            front = {
                primitive 'key',
                symbol { type = text ': ' },
                symbol { type = space {}, tags = { 'split', 'record_table' } },
                atom 'value',
            },
            precedence = 100,
        },
        {
            id = 'luxem_array',
            name = 'Luxem Array',
            back = { data_array 'data' },
            middle = {
                data = array {
                    type = 'value',
                }
            },
            alignments = {
                base = relative { base = 'indent', offset = 0 },
                indent = relative { base = 'indent', offset = 16 },
            },
            front = {
                symbol { type = text '[' },
                array {
                    middle = 'data',
                    prefix = { { type = space {}, tags = { 'indent', 'split' } } },
                    separator = { { type = text ', ' } },
                },
                symbol { type = text ']', tags = { 'split', 'base' }, }
            },
            auto_choose_ambiguity = 999,
            precedence = 0,
        },
        {
            id = 'luxem_primitive',
            name = 'Luxem Primitive',
            back = { data_primitive 'data' },
            middle = { data = primitive {} },
            front = {
                primitive 'data',
            },
        }
    },
    modules = {
        selection_type {},
        _indicators,
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
    },
}
