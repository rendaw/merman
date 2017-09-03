function _c(r, g, b)
    return rgb { r = r / 255, g = g / 255, b = b / 255 }
end

function _c2(r, g, b, a)
    return rgba { r = r / 255, g = g / 255, b = b / 255, a = a / 255 }
end

return {
    black = _c(0, 0, 0),
    darkblue = _c(17, 21, 34),
    midblue = _c(80, 112, 151),
    lightblue = _c(184, 191, 210),
    darkgreen = _c(37, 63, 57),
    lightgreen = _c(56, 118, 83),
    red = _c(242, 80, 27),
    lightred = _c2(242, 80, 27, 77),
}
