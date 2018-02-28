
local boomOptions = { 
	width=128, 
	height=128, 
	numFrames=64,
	sheetContentWidth=1024,
	sheetContentHeight=1024
}

local boomSheets = {
	graphics.newImageSheet("assets/images/boom_0.png", boomOptions),
	graphics.newImageSheet("assets/images/boom_1.png", boomOptions),
	graphics.newImageSheet("assets/images/boom_2.png", boomOptions)
}

local boomSequences = { {
    start = 1,
    count = 64,
    time = 500,
    -- loopCount = 1
} }

---------------------------------------------------------------------------------

local exports = {}

exports.newSprite = function(parent)
	return display.newSprite(parent, boomSheets[math.random(#boomSheets)], boomSequences)
end

return exports
