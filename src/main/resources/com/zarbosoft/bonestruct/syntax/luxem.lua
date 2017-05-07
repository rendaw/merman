return {
    name = 'luxem',
    pad_converse = 5,
    pad_transverse = 60,
    background = rgb { r = 74 / 255, g = 60 / 255, b = 89 / 255 },
    animate_course_placement = true,
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
            with = { part 'select' },
            obbox = {
                line_color = rgb { r = 101 / 255, g = 82 / 255, b = 122 / 255 },
            },
        },
        {
            with = { free 'break', state 'compact' },
            ['break'] = true,
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
            with = { free 'record_table', state 'compact' },
            align = 'record_table',
        },
        {
            with = { part 'primitive', state 'hard' },
            ['break'] = true,
        },
        {
            with = { part 'primitive', state 'soft' },
            ['break'] = true,
        }
    },
    groups = {
        value = {
            'luxem_object',
            'luxem_array',
            'luxem_primitive',
        },
    },
    root = 'value',
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
                    middle = 'data',
                    separator = { text ',' },
                },
                symbol { type = text '}', tags = { 'break', 'base' }, },
            },
            auto_choose_ambiguity = 999,
        },
        {
            id = 'luxem_object_element',
            name = 'Luxem Object Element',
            back = { data_key 'key', data_node 'value' },
            middle = {
                key = primitive {},
                value = node 'value',
            },
            front = {
                symbol { type = space {}, tags = { 'indent', 'break' } },
                primitive 'key',
                symbol { type = text ':' },
                symbol { type = space {}, tags = { 'record_table' } },
                node 'value',
            },
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
                symbol { type = text '{' },
                array {
                    middle = 'data',
                    prefix = { { type = space {}, tags = { 'indent', 'break' } } },
                    separator = { { type = text ',' } },
                },
                symbol { type = text '}', tags = { 'break', 'base' }, }
            },
            auto_choose_ambiguity = 999,
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
        hover_type {},
        modes {
            states = {
                'nottyping',
                'typing',
            },
        },
        indicators {
            indicators = {
                {
                    id = 'typing',
                    symbol = text 't',
                    tags = { global 'mode_typing' },
                },
                {
                    id = 'nottyping',
                    symbol = text 'n',
                    tags = { global 'mode_nottyping' },
                },
            },
        },
        hotkeys {
            rules = {
                {
                    with = { global 'mode_nottyping' },
                    hotkeys = {
                        exit = { key 'left', key 'h' },
                        enter = { key 'right', key 'l' },
                        next = { key 'down', key 'j' },
                        previous = { key 'up', key 'k' },
                        delete = { key 'x' },
                    },
                    free_typing = false,
                },
                {
                    with = { global 'mode_nottyping', part 'primitive' },
                    hotkeys = {
                        mode_typing = { key 'right', key 'l' },
                    }
                },
                {
                    with = { global 'mode_typing', part 'primitive' },
                    hotkeys = {
                        mode_nottyping = { key 'escape' },
                        next = { key 'right' },
                        previous = { key 'left' },
                        delete_next = { key 'delete' },
                        delete_previous = { key 'backspace' },
                    }
                },
            },
        },
    },
}
