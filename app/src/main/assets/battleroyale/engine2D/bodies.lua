module (..., package.seeall)

local BODY_TYPE_WALL 			= 'wall'
local BODY_TYPE_MY_PLAYER 		= 'myPlayer'
local BODY_TYPE_OTHER_PLAYER 	= 'otherPlayer'
local BODY_TYPE_MY_BULLET 		= 'myBullet'
local BODY_TYPE_OTHER_BULLET 	= 'otherBullet'
local BODY_TYPE_ITEM 			= 'item'

local physics  = require 'physics'
local emitter  = require 'emitter'
local vibrator = require 'vibrator'
local sounds   = require 'sounds'
local bonusLibrary = require 'bonus.bonus'

-- COLLISIONS
----------------------------------------------------------------------------------------------------------------
-- 					|	1	|	2	|	4	|	8	|	16	|	32	|	64	|	128    |	256	|	512	|	SUM	|
----------------------------------------------------------------------------------------------------------------
-- 1				|	x 																					1
-- Wall 			|			x 				x 		x													26
----------------------------------------------------------------------------------------------------------------
-- 2				|	 		x																			2
-- My Player 		|	x		 		x		 		x		x											53
----------------------------------------------------------------------------------------------------------------
-- 4				|			 		x																	4
-- Other Players 	|			x																			2
----------------------------------------------------------------------------------------------------------------
-- 8				|					 		x															8
-- My Bullets 		| 	x																					1
----------------------------------------------------------------------------------------------------------------
-- 16				|							 		x													16
-- Other Bullets 	| 	x		x																			3
----------------------------------------------------------------------------------------------------------------
-- 32				|									 		x											32
-- Items 			|			x																			2
----------------------------------------------------------------------------------------------------------------

local wallCollisionFilter 			= { categoryBits = 1, 	maskBits = 26 }
local myPlayerCollisionFilter 		= { categoryBits = 2, 	maskBits = 53 }
local otherPlayersCollisionFilter 	= { categoryBits = 4, 	maskBits = 2 }
local myBulletsCollisionFilter 		= { categoryBits = 8, 	maskBits = 1 }
local otherBulletsCollisionFilter 	= { categoryBits = 16, 	maskBits = 3 }
local itemsCollisionFilter 			= { categoryBits = 32, 	maskBits = 2 }

local exports = {}

local bodies = {}

local function scaleDownAndRemove(target) 
	transition.scaleTo(target, { xScale = 0.01, yScale = 0.01, time = 400, transition = easing.outQuad, onComplete = function()
 		display.remove(target)
	end })
end

exports.addWall = function(wall)
	physics.addBody(wall, 'static', { filter = wallCollisionFilter })
	wall.isSensor = false
	wall.type = BODY_TYPE_WALL
end

-- PLAYERS

exports.addMyPlayer = function(myPlayer)
	physics.addBody(myPlayer, { radius = 29, filter = myPlayerCollisionFilter })
	myPlayer.linearDamping = 5
	myPlayer.type = BODY_TYPE_MY_PLAYER
end

exports.addOtherPlayer = function(otherPlayer, onComplete)
	physics.addBody(otherPlayer, { radius = 29, filter = otherPlayersCollisionFilter })
	otherPlayer.isSensor = true
	otherPlayer.type = BODY_TYPE_OTHER_PLAYER
	otherPlayer:addEventListener('collision', function (event)
		if event.other.type == BODY_TYPE_MY_PLAYER then
			onComplete()
		end
	end)
end

-- BULLETS

exports.addMyBullet = function(id, myBullet)
	physics.addBody(myBullet, { radius = 4, bounce = 0, filter = myBulletsCollisionFilter })
	myBullet.isBullet 	= true
	myBullet.id 		= id
	myBullet.type 		= BODY_TYPE_MY_BULLET
end

exports.addOtherBullet = function(id, otherBullet)
	physics.addBody(otherBullet, { radius = 4, bounce = 0, filter = otherBulletsCollisionFilter })
	otherBullet.isBullet = true
	otherBullet.id 		= id
	otherBullet.type 	= BODY_TYPE_OTHER_BULLET
end


exports.addBullet = function(gameGroup, gamemaster, myPlayer, byPlayer, id, x, y, angle, weapon)	
	local bullet = display.newImageRect(gameGroup, weapon.spriteBulletPrefix .. byPlayer.color .. '.png', weapon.bulletSize, weapon.bulletSize)
	bullet.x, bullet.y = x, y
	bullet.isBullet = true
	local deltaX = weapon.speed * math.sin(angle)
	local deltaY = weapon.speed * -math.cos(angle)

	if myPlayer and myPlayer.id == byPlayer.id then
		exports.addMyBullet(id, bullet)
	else
		exports.addOtherBullet(id, bullet)
	end

	bodies[id] = bullet

	bullet:addEventListener('collision', function (event)

		if event.other.type == BODY_TYPE_MY_PLAYER and myPlayer then
			gamemaster.hitByBullet(id, weapon.pts, myPlayer:scoreByAddingPoints(weapon.pts)) -- pts is negative here
			myPlayer:shot()
			vibrator.sendImpact()
			sounds.playShotByBullet()
		end

		-- Remove the bullet from game
		display.remove(bullet)
	end)

	local ratio = 1000
	bullet:applyForce(deltaX / ratio, deltaY / ratio, bullet.x, bullet.y)

end

exports.addBonus = function(gameGroup, x, y, type, subtype, id)
	local item = bonusLibrary.getBonus(type, subtype)
	local bonus = display.newImageRect(item.emoji.image, item.emoji.width, item.emoji.height)
	bonus.x, bonus.y = x, y
	bonus.bonusType = type
	bonus.bonusSubtype = subtype
	bonus.id = id
	bonus.item = item

	local color = {1, 1, 1}
	bonus.emitter = emitter.newBonusEnabledEmitter(color)
	gameGroup:insert(bonus.emitter)
	bonus.emitter.x, bonus.emitter.y = bonus.x, bonus.y

	gameGroup:insert(bonus)

	bodies[id] = bonus

	physics.addBody(bonus, { radius = 28, filter = itemsCollisionFilter })
	bonus.isSensor = true
	bonus.type = BODY_TYPE_ITEM

	return bonus
end

exports.getBody = function(id)
	return bodies[id]
end

exports.removeBody = function(id)
	local body = bodies[id]
	if body then
		display.remove(body.emitter)
		scaleDownAndRemove(body)
	end

	bodies[id] = nil
end

return exports