module (..., package.seeall)

local widget = require 'widget'

function newMiniMap(parameters)

	local s = { width=110, height=70, radius=13, margin=10 }

	local worldWidth = parameters.worldWidth
	local worldHeight = parameters.worldHeight
	
	local mapWidth
	local mapHeight

	local widthRatio
	local heightRatio
	
	if parameters.fixedWidth then
		mapWidth = parameters.fixedWidth
		mapHeight = worldHeight * mapWidth / worldWidth
	elseif parameters.fixedHeight then
		mapHeight = parameters.fixedHeight
		mapWidth = worldWidth * mapHeight / worldHeight
	end

	widthRatio  = (mapWidth  - s.margin * 2) / worldWidth
	heightRatio = (mapHeight - s.margin * 2) / worldHeight

	local sheet = graphics.newImageSheet('assets/images/mini_map.png', {
		frames = {
			{ x=0,  			   y=0, 			    width=s.radius, height=s.radius },
			{ x=s.radius, 		   y=0, 			    width=1,  		height=s.radius },
			{ x=s.width-s.radius,  y=0, 			    width=s.radius, height=s.radius },
			{ x=0,  			   y=s.radius, 		    width=s.radius, height=1 },
			{ x=s.radius,  	       y=s.radius, 		    width=1, 		height=1 },
			{ x=s.width-s.radius,  y=s.radius, 		    width=s.radius, height=1 },
			{ x=0,  			   y=s.height-s.radius, width=s.radius, height=s.radius },
			{ x=s.radius, 		   y=s.height-s.radius, width=1,  		height=s.radius },
			{ x=s.width-s.radius,  y=s.height-s.radius, width=s.radius, height=s.radius }
		},
		sheetContentWidth  = s.width,
	    sheetContentHeight = s.height
	})

	local button = widget.newButton({
			width  = mapWidth,
			height = mapHeight,
			sheet  = sheet,       
			topLeftFrame            = 1,
	        topMiddleFrame          = 2,
	        topRightFrame           = 3,
	        middleLeftFrame         = 4,
	        middleFrame             = 5,
	        middleRightFrame        = 6,
	        bottomLeftFrame         = 7,
	        bottomMiddleFrame       = 8,
	        bottomRightFrame        = 9,
	        topLeftOverFrame        = 1,
	        topMiddleOverFrame      = 2,
	        topRightOverFrame       = 3,
	        middleLeftOverFrame     = 4,
	        middleOverFrame         = 5,
	        middleRightOverFrame    = 6,
	        bottomLeftOverFrame     = 7,
	        bottomMiddleOverFrame   = 8,
	        bottomRightOverFrame    = 9,
	})

	button:setEnabled(false)
	button.anchorY = 0
	button.x, button.y = display.contentWidth/2, safeScreenOriginY + 16
	button.margin = s.margin
	button.radius = s.radius

	function button:convert(x, y) 
		return s.margin + x * widthRatio, s.margin + y * heightRatio
	end

	return button
end
