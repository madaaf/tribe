
-- DIMENSIONS

local worldSize = { width=1000, height=1000 }

-- Create a vector rectangle
local rect = display.newRect(0, 0, 2000, 2000)

display.setDefault( "textureWrapX", "repeat" )
display.setDefault( "textureWrapY", "repeat" )

rect.fill = { type = 'image', filename = 'assets/images/bg_world.png' }
rect.fill.scaleX = 128 / rect.width
rect.fill.scaleY = 128 / rect.height

local map = display.newRoundedRect(100, 100, 512, 512, 8)
map.anchorX, map.anchorY = 0, 0
map.fill = { type = 'image', filename = 'assets/images/bg_map.png' }
map.fill.scaleX = 128 / map.width
map.fill.scaleY = 128 / map.height
map:setStrokeColor(173/255, 174/255, 175/255, 0.14)
map.strokeWidth = 3

display.setDefault( "textureWrapX", "clampToEdge" )
display.setDefault( "textureWrapY", "clampToEdge" )

local mapShadow = display.newImageRect('assets/images/bg_map_shadow.png', 612, 612)
mapShadow.anchorX, mapShadow.anchorY = 0, 0
mapShadow.x, mapShadow.y = 50,50

map:toFront()