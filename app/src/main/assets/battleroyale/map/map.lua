module (..., package.seeall)

local widget = require 'widget'

function newMap(parameters)

	local innerWallSize = 3
	local shadowOffset = 120

	local mapGroup = display.newGroup()

	display.setDefault( "textureWrapX", "repeat" )
	display.setDefault( "textureWrapY", "repeat" )

	local screenW, screenH = display.contentWidth, display.contentHeight

	local rect = display.newRect(mapGroup, -screenW, -screenH, parameters.width + screenW * 2, parameters.height + screenH * 2)
	rect.anchorX, rect.anchorY = 0,0
	rect.fill = { type = 'image', filename = 'assets/images/bg_world.png' }
	rect.fill.scaleX = 128 / rect.width
	rect.fill.scaleY = 128 / rect.height

	local map = display.newRoundedRect(mapGroup, -parameters.radius/2 - innerWallSize, -parameters.radius/2 - innerWallSize, parameters.width + innerWallSize * 2, parameters.height + innerWallSize * 2, parameters.radius)
	map:setStrokeColor(173/255, 174/255, 175/255, 0.14)
	map.strokeWidth = innerWallSize
	map.anchorX, map.anchorY = 0,0

	map.fill = { type = 'image', filename = 'assets/images/bg_map.png' }
	map.fill.scaleX = 128 / map.width
	map.fill.scaleY = 128 / map.height

	display.setDefault( "textureWrapX", "clampToEdge" )
	display.setDefault( "textureWrapY", "clampToEdge" )

	local mapShadow = display.newImageRect(mapGroup, 'assets/images/bg_map_shadow.png', map.width + shadowOffset * 2, map.height + shadowOffset * 2)
	mapShadow.anchorX, mapShadow.anchorY = 0, 0
	mapShadow.x, mapShadow.y = map.x - shadowOffset, map.y - shadowOffset

	map:toFront()

	return mapGroup
end
