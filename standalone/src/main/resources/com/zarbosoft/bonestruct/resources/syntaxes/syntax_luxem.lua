local _hotkeys = require 'hotkeys'

local syntax = {
    name = 'luxem',
    pad = {
        converse_start = 15,
        converse_end = 5,
        transverse_start = 60,
        transverse_end = 60,
    },
    banner_pad = {
        converse_start = 15,
    },
    detail_pad = {
        converse_start = 15,
    },
    background = rgb { r = 74 / 255, g = 60 / 255, b = 89 / 255 },
    animate_course_placement = true,
    pretty_save = true,
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
    root = {
        middle = {
            data = array {
                type = 'value'
            }
        },
        back = { root_data_array 'data' },
        front = {
            array {
                middle = 'data',
                prefix = { { type = space {}, tags = { 'split' } } },
                separator = { { type = text ',' } },
            },
        },
        alignments = {
            indent = absolute { offset = 0 },
        },
    },
    gap = {
        prefix = {
            text '◇',
        },
    },
    prefix_gap = {
        prefix = {
            text '⬗',
        },
    },
    suffix_gap = {
        prefix = {
            text '⬖',
        },
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
            depth_score = 1,
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
            depth_score = 1,
        },
        {
            id = 'luxem_primitive',
            name = 'Luxem Primitive',
            back = { data_primitive 'data' },
            middle = { data = primitive {} },
            front = {
                primitive 'data',
            },
            auto_choose_ambiguity = 1,
        }
    },
    modules = {
        selection_type {},
        indicators {
            indicators = {
                {
                    id = 'window',
                    symbol = text 'w',
                    tags = { global 'window' },
                },
            },
        },
    },
}

_hotkeys.create():apply(syntax)

return syntax
