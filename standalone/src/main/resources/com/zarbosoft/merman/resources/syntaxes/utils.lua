local module = {}

function module.append(table, value)
    table[#table + 1] = value
end

function module.append_multiple(table, values)
    for i, v in pairs(values) do
        module.append(table, v)
    end
end

return module
