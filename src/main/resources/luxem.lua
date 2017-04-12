return {
    name = 'luxem',
    background = { 0.21568, 0.21568, 0.21568 },
    animate_course_placement = true,
    hover_style = {
        line_color = { 0.21568, 0.05, 0.43 },
    },
    styles = {
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
            id = '__gap',
            name = 'Gap',
            back = {
                type '__gap',
                data_primitive 'value',
            },
            middle = { value = primitive {} },
            front = {
                mark '*',
                gap 'value',
            },
        },
        {
            id = 'luxem_object',
            name = 'Luxem Object',
            back = { data_record 'data' },
            middle = {
                data = record {
                    tag = 'value',
                }
            },
            alignments = {
                base = relative { base = 'indent', offset = 0 },
                indent = relative { base = 'indent', offset = 16 },
                record_table = concensus {},
            },
            front = {
                mark '{',
                record {
                    middle = data,
                    prefix = { space { tags = { 'indent', 'break' } } },
                    infix = {
                        mark ':',
                        space { tags = { 'record_table' } },
                    },
                    separator = { mark ',' },
                },
                mark { value = '}', tags = { 'break', 'base' }, },
            },
        },
        {
            id = 'luxem_array',
            name = 'Luxem Array',
            back = { data_array 'data' },
            middle = {
                data = array {
                    tag = 'value',
                }
            },
            alignments = {
                base = relative { base = 'indent', offset = 0 },
                indent = relative { base = 'indent', offset = 16 },
            },
            front = {
                mark '{',
                array {
                    middle = data,
                    prefix = { space { tags = { 'indent', 'break' } } },
                    separator = { mark ',' },
                },
                mark { value = '}', tags = { 'break', 'base' }, }
            },
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
    plugins = {
        hover_type {},
        mode {
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
                        delete_next = { key 'x' },
                    }
                },
                {
                    tags = { global 'mode_nottyping', part 'primitive' },
                    hotkeys = {
                        set_mode_typing = { key 'right' },
                    }
                },
                {
                    tags = { global 'mode_typing', part 'primitive' },
                    hotkeys = {
                        set_mode_nottyping = { key 'escape' },
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
