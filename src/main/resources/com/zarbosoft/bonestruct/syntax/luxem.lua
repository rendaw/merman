return {
    name = 'luxem',
    pad_converse = 5,
    pad_transverse = 60,
    background = rgb { r = 74 / 255, g = 60 / 255, b = 89 / 255 },
    animate_course_placement = true,
    hover_style = {
        line_color = rgb { r = 112 / 255, g = 80 / 255, b = 145 / 255 },
    },
    select_style = {
        line_color = rgb { r = 101 / 255, g = 82 / 255, b = 122 / 255 },
    },
    styles = {
        {
            color = rgb { r = 163 / 255, g = 164 / 255, b = 232 / 255 },
            font_size = 16,
        },
        {
            tags = { part 'details' },
            font_size = 12,
        },
        {
            tags = { part 'banner' },
            font_size = 12,
        },
        {
            tags = { part 'primitive' },
            color = rgb { r = 55 / 255, g = 83 / 255, b = 232 / 255 },
        },
        {
            tags = { part 'gap' },
            color = rgb { r = 255 / 255, g = 249 / 255, b = 106 / 255 },
        },
        {
            tags = { free 'break', state 'compact' },
            ['break'] = true,
        },
        {
            tags = { free 'base', state 'compact' },
            align = 'base',
        },
        {
            tags = { free 'indent', state 'compact' },
            align = 'indent',
        },
        {
            tags = { free 'record_table', state 'compact' },
            align = 'record_table',
        },
        {
            tags = { part 'primitive', state 'hard' },
            ['break'] = true,
        },
        {
            tags = { part 'primitive', state 'soft' },
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
                mark '{',
                array {
                    middle = 'data',
                    separator = { mark ',' },
                },
                mark { value = '}', tags = { 'break', 'base' }, },
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
                space { tags = { 'indent', 'break' } },
                primitive 'key',
                mark ':',
                space { tags = { 'record_table' } },
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
                mark '{',
                array {
                    middle = 'data',
                    prefix = { space { tags = { 'indent', 'break' } } },
                    separator = { mark ',' },
                },
                mark { value = '}', tags = { 'break', 'base' }, }
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
        hotkeys {
            rules = {
                {
                    tags = { global 'mode_nottyping' },
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
                    tags = { global 'mode_nottyping', part 'primitive' },
                    hotkeys = {
                        mode_typing = { key 'right', key 'l' },
                    }
                },
                {
                    tags = { global 'mode_typing', part 'primitive' },
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
