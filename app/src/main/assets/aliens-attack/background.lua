local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local group, currentBackgroundGroup

local ground, city
local currentGradientType = 0

local timers = {}

local model   = require 'model'
local emitter = require 'emitter'

local groundHeight = 90
local backgroundImageHeight = 542

---------------------------------------------------------------------------------

local function loadColorBackground() 
	log('loadColorBackground')

	local background = display.newImageRect("assets/images/bg_0_color.png", screenW, screenH - backgroundImageHeight)
	background.anchorX = 0 
	background.anchorY = 0

	backgroundGroup:insert(background)
end

local function loadGradient() 
	log('loadGradient')

	local h = backgroundImageHeight

	local gradient = display.newImageRect("assets/images/bg_0.jpg", screenW, h)
	gradient.anchorX = 0 
	gradient.anchorY = 0
	gradient.y = screenH - h

	backgroundGroup:insert(gradient)
end

local function loadCity()
	log('loadCity')

	local h = screenW * 204 / 320 

	city = display.newImageRect("assets/images/city.png", screenW, h)
	city.anchorX = 0
	city.anchorY = 0
	city.y = screenH - groundHeight - h

	group:insert(city)
end

local function loadFog()
	log('loadFog')

	local fog = emitter.newFogEmitter()
	fog.anchorX = 0
	fog.anchorY = 1
	fog.y = screenH - groundHeight

	group:insert(fog)
end

local function loadStars()
	log('loadStars')

	local stars = emitter.newStarsEmitter()
	stars.anchorX = 0
	stars.anchorY = 0

	group:insert(stars)
end

local function popCar()
	log('popCar')

	local car = display.newImageRect( "assets/images/car.png", 31, 15 )
	backgroundGroup:insert(car)

	car.x, car.y = -car.width, screenH - groundHeight
	car.anchorY = 1

	transition.to( car, { time=3000, x=screenW+car.width } )
end

local function popStars()
	log('popStars')

	for i=1,5 do
		local star = display.newImageRect( "assets/images/star.png", 16, 16 )
		backgroundGroup:insert(star)

		star.x, star.y = math.random(50, screenW-100), math.random(100, screenH/2)
		star.alpha = 0

		transition.fadeIn(star, { delay=math.random(1,3) * 1000 })

		local removeStar = function () display.remove(star) end
		transition.fadeOut(star, { delay=math.random(7,9) * 1000, onComplete=removeStar })
	end
end

local function popSmallCloud()
	log('popSmallCloud')

	local cloudType = math.random(0,2)
	local cloud
	if cloudType == 0 then
		cloud = display.newImageRect( "assets/images/small_cloud_0.png", 30, 21 )
	elseif cloudType == 1 then
		cloud = display.newImageRect( "assets/images/small_cloud_1.png", 30, 21 )
	else 
		cloud = display.newImageRect( "assets/images/small_cloud_2.png", 30, 24 )
	end
	backgroundGroup:insert(cloud)

	cloud.x, cloud.y = -cloud.width, math.random(0, 150)
	cloud.alpha = math.random()

	local removeCloud = function () display.remove(cloud) end
	transition.to( cloud, { time=math.random(3000, 5000), x=screenW+cloud.width, onComplete=removeCloud } )
end

local function popCloud()
	log('popCloud')

	local cloudType = math.random(0,2)
	local cloud
	if cloudType == 0 then
		cloud = display.newImageRect( "assets/images/cloud_0.png", 164, 112 )
	elseif cloudType == 1 then
		cloud = display.newImageRect( "assets/images/cloud_1.png", 145, 98 )
	else 
		cloud = display.newImageRect( "assets/images/cloud_2.png", 206, 158 )
	end
	backgroundGroup:insert(cloud)

	cloud.x, cloud.y = -cloud.width, math.random(-100, 150)
	cloud.alpha = math.random() * 0.3

	local removeCloud = function () display.remove(cloud) end
	transition.to( cloud, { time=math.random(3000, 5000), x=screenW+cloud.width, onComplete=removeCloud } )
end

local function changeGradientType(gradientType)
	log('changeGradientType')

	if not (currentGradientType == gradientType) then
		currentGradientType = gradientType

		local newBackgroundGroup = display.newGroup()
		newBackgroundGroup.isVisible = false
		group:insert(newBackgroundGroup)
		newBackgroundGroup:toBack()
		newBackgroundGroup.isVisible = true

		-- Gradient

		local newGradient = display.newImageRect("assets/images/bg_" .. gradientType .. ".jpg", screenW, backgroundImageHeight)
		newGradient.anchorX = 0 
		newGradient.anchorY = 0
		newGradient.y = screenH - backgroundImageHeight

		newBackgroundGroup:insert(newGradient)

		local oldGradient = currentGradient
		currentGradient = newGradient


		-- Color

		local newBackground = display.newImageRect("assets/images/bg_0_color.png", screenW, newGradient.y)
		newBackground.anchorX = 0 
		newBackground.anchorY = 0

		newBackgroundGroup:insert(newBackground)
		newBackground:toBack()

		local oldBackgroundGroup = backgroundGroup
		backgroundGroup = newBackgroundGroup

		transition.fadeOut(oldBackgroundGroup, { onComplete=function () display.remove(oldBackgroundGroup) end })
	end
end

local function startAnimating()
	log('startAnimating')

	table.insert(timers, timer.performWithDelay( 2000, popCar,        -1))
	-- table.insert(timers, timer.performWithDelay(10000, popStars,      -1))
	table.insert(timers, timer.performWithDelay( 1000, popSmallCloud, -1))
	table.insert(timers, timer.performWithDelay( 2000, popCloud,      -1))

	-- popStars()
end

local function stopAnimating()
	log('stopAnimating')

	for i,t in ipairs(timers) do
		timer.cancel(t)
	end

	timers = {}
end

---------------------------------------------------------------------------------

local exports = {}

exports.shake = function() 

	transition.to(city, { x=-40, time=150, onComplete=function ()
		transition.to(city, { x=40, time=150, onComplete=function ()
			transition.to(city, { x=-40, time=150, onComplete=function ()
				transition.to(city, { x=0, time=150 })
			end})
		end})
	end})
end

exports.load = function()
	group = display.newGroup()
	backgroundGroup = display.newGroup()
	group:insert(backgroundGroup)

	loadColorBackground()
	loadGradient()
	loadCity()
	loadFog()
	loadStars()
	startAnimating()

	return group
end

exports.resetGradient = function()
	log('resetGradient')

	changeGradientType(0)
end

exports.switchGradient = function(score)
	log('switchGradient')

	local level = model.levelByScore(score)
	changeGradientType(level.background)
end

exports.dispose = function()
	stopAnimating()
end

return exports
