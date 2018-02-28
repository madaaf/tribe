local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local group 

local currentGradient, ground, city
local currentGradientType = 0

local timers = {}

local model = require 'model'

---------------------------------------------------------------------------------

local function loadGradient() 
	log('loadGradient')

	local gradient = display.newImageRect("assets/images/bg_0.jpg" , screenW, screenH)
	gradient.anchorX = 0 
	gradient.anchorY = 0

	group:insert(gradient)

	currentGradient = gradient
end

local function loadCity()
	log('loadCity')

	local h = screenW * 204 / 320 

	city = display.newImageRect("assets/images/city.png", screenW, h)
	city.anchorX = 0
	city.anchorY = 0
	city.y = screenH * 0.85 - h

	group:insert(city)
end

local function popCar()
	log('popCar')

	local car = display.newImageRect( "assets/images/car.png", 31, 15 )
	car.x, car.y = -car.width, screenH * 0.85
	car.anchorY = 1

	transition.to( car, { time=3000, x=screenW+car.width } )
end

local function popStars()
	log('popStars')

	for i=1,5 do
		local star = display.newImageRect( "assets/images/star.png", 16, 16 )
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

	cloud.x, cloud.y = -cloud.width, math.random(-100, 150)
	cloud.alpha = math.random() * 0.3

	local removeCloud = function () display.remove(cloud) end
	transition.to( cloud, { time=math.random(3000, 5000), x=screenW+cloud.width, onComplete=removeCloud } )
end

local function changeGradientType(gradientType)
	log('changeGradientType')

	if not (currentGradientType == gradientType) then
		currentGradientType = gradientType

		local newGradient = display.newImageRect("assets/images/bg_" .. gradientType .. ".jpg", screenW, screenH)
		newGradient.anchorX = 0 
		newGradient.anchorY = 0
		
		newGradient.isVisible = false
		group:insert(newGradient)
		newGradient:toBack()
		newGradient.isVisible = true

		local oldGradient = currentGradient
		currentGradient = newGradient

		transition.fadeOut(oldGradient, { onComplete=function () display.remove(oldGradient) end })
	end
end

local function startAnimating()
	log('startAnimating')

	table.insert(timers, timer.performWithDelay( 2000, popCar,        -1))
	table.insert(timers, timer.performWithDelay(10000, popStars,      -1))
	table.insert(timers, timer.performWithDelay( 1000, popSmallCloud, -1))
	table.insert(timers, timer.performWithDelay( 2000, popCloud,      -1))

	popStars()
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

	transition.to(city, { x=-20, time=150, onComplete=function ()
		transition.to(city, { x=20, time=150, onComplete=function ()
			transition.to(city, { x=-20, time=150, onComplete=function ()
				transition.to(city, { x=0, time=150 })
			end})
		end})
	end})
end

exports.load = function()
	group = display.newGroup()

	loadGradient()
	loadCity()
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
