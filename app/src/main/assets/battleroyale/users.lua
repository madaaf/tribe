local colors 		= require 'colors'
local bodiesLibrary = require 'engine2D.bodies'
local emitter 	    = require 'emitter'
local texts 	    = require 'texts'
local crypto 		= require 'crypto'
local gamemaster 	= require 'gamemaster'
local bonusLibrary 	= require 'bonus.bonus'
local inspect 		= require 'inspect'
local sounds 		= require 'sounds'
local joystickLibrary = require 'joysticks.joysticks'
local utils			  = require 'utils.utils'

local screenW, screenH = display.contentWidth, display.contentHeight

local players = {}
local peers = {}
local isDead = {}

local function naiveHash(str)

	local sum = 0
	for i=1,#str do
		sum = sum + str:byte(i)
	end

	return sum
end

local function broadcastStatuses()

	local statuses = {}

	for k,v in pairs(peers) do
		if not players[v.id] then
			if isDead[v.id] then
				statuses[v.id] = '☠️'
			else
				statuses[v.id] = '⏳'
			end
		end
	end

	Runtime:dispatchEvent({ name='coronaView', event='statusesUpdated', statuses=statuses })
end

local function lerp( v0, v1, t )
    return v0 + t * (v1 - v0)
end

local function scaleDown(target) 
	transition.scaleTo(target, { xScale = 0.01, yScale = 0.01, time = 400, transition = easing.outQuad })
end

local function scaleUp(target, onComplete) 
	target.xScale, target.yScale, alpha = 0.01, 0.01, 0
	transition.scaleTo(target, { xScale = 1, yScale = 1, alpha = 1, time = 400, transition = easing.outQuad, onComplete = onComplete })
end

local exports = {}

exports.myUserId = nil
exports.myPlayer = nil

exports.resetStatuses = function ()
	isDead = {}
	broadcastStatuses()
end

exports.isAlone = function ()
	return utils.tablelength(peers) < 2
end

exports.updatePeers = function (session)
	if session and session.peers then
		peers = session.peers
	end
	broadcastStatuses()
end

exports.addPeer = function(peer)
	peers[peer.id] = peer
	broadcastStatuses()
end

exports.getPlayer = function(playerId) 
	return players[playerId]
end

exports.removeAllPlayers = function() 
	log('Corona - removeAllPlayers')
	exports.myPlayer = nil
	for k,v in pairs(players) do
		v:hide() -- TODO : Scale down
	end
	players = {}
end

exports.showPlayingPlayers = function(playersGroup, miniMap, camera, game) 
	log('Corona - showPlayingPlayers')

	local defaultWeapon = bonusLibrary.defaultWeapon()

	for k,v in pairs(game.players) do
		local player = exports.createPlayer(playersGroup, miniMap, camera, v)
		player:setWeapon(defaultWeapon, false)
		player:updateScore(v.score, false)
		player.score.alpha = 1
		player.name.alpha = 1
		player.nameBg.alpha = 1
	end

	exports.followBestRemainingPlayer(camera)
end

exports.showStartingPlayers = function(playersGroup, miniMap, camera, game) 
	log('Corona - showStartingPlayers')

	local myPos = { 500, 500 }
	for k,v in pairs(game.players) do
		if v.id == exports.myUserId then
			myPos = v.pos
			exports.createPlayer(playersGroup, miniMap, camera, v)
		end
	end

	local margin = 80
	local playersPos = {
		{ myPos[1]         , myPos[2] + margin },
		{ myPos[1]         , myPos[2] - margin },
		{ myPos[1] - margin, myPos[2]          },
		{ myPos[1] + margin, myPos[2]          },
		{ myPos[1] - margin, myPos[2] - margin },
		{ myPos[1] + margin, myPos[2] + margin },
		{ myPos[1] + margin, myPos[2] - margin },
		{ myPos[1] - margin, myPos[2] + margin },
	}

	local posIndex = 1
	for k,v in pairs(game.players) do
		if v.id ~= exports.myUserId then
			local v2 = {
				id = v.id,
				color = v.color,
				pos = playersPos[posIndex]
			}
			exports.createPlayer(playersGroup, miniMap, camera, v2)
			posIndex = posIndex + 1
		end
	end

	timer.performWithDelay(2200, function ()

		for k,v in pairs(game.players) do

			local player = players[v.id]
			if player then
				player:start(v)
			end
		end
	end)
end

exports.createTrainingPlayer = function (playersGroup, miniMap, camera)
	return exports.createPlayer(playersGroup, miniMap, camera, {
		id = exports.myUserId,
		color = 'white',
		pos = { 500, 500 }
	})
end

exports.createPlayer = function (playersGroup, miniMap, camera, parameters)
	log('createPlayer')
	
	if not players[parameters.id] then
		local color = colors(parameters.color)
		local displayName = 'Anonymous'
		
		local peer = peers[parameters.id]
		if peer and peer.display_name then
			displayName = peer.display_name
		end

		local deltaX = 0
		local deltaY = 0
		local playerBackground = display.newImageRect(color.image, 80, 80)
		local playerImage = display.newCircle(0, 0, 22, 22)
		playerImage:setFillColor(unpack(color.rgba))
		local playerMiniImage = display.newImageRect(color.miniImage, 27, 27)
		local playerName = display.newText(displayName:upper(), 0, 0, 'ProximaNovaSoft-Bold', 8)
		playerName.alpha = 0
		local playerScore = display.newText('', 0, 0, 'Circular', 19)
		playerScore.alpha = 0
		local playerNameBackground = display.newRoundedRect(0,0,playerName.width + 12, 16, 8)
		playerNameBackground.alpha = 0
		playerNameBackground:setFillColor(unpack(color.rgba))
		local bonusEnabled = emitter.newBonusEnabledEmitter(color.rgba)
		bonusEnabled.alpha = 0

		local player = { id = parameters.id, display_name = displayName, name = playerName, nameBg = playerNameBackground, weapon = {}, score = playerScore, image = playerImage, 
		miniImage = playerMiniImage, bg = playerBackground, color = parameters.color, scoreValue = 0, bonusEnabled = bonusEnabled }

		function player:scoreByAddingPoints(pts, animate)
			self:updateScore(self.scoreValue + pts, animate)
			return self.scoreValue
		end

		function player:updateScore(newScore, animate)
			local oldScore = self.scoreValue
			self.scoreValue = math.max(0, newScore)

			if oldScore ~= self.scoreValue then
				if self.scoreValue == 0 then
					self:die()
					if self.id == exports.myUserId then
						joystickLibrary:hide()
						exports.myPlayer = nil
						timer.performWithDelay(500, function ()
							gamemaster.leavePlayers()
						end)
					end
				else
					if animate then
						self:animateScore(newScore - oldScore, 1000, oldScore)
					else
						self.score.text = self.scoreValue
					end
				end
			end
		end

		function player:start(parameters)

			local defaultWeapon = bonusLibrary.defaultWeapon()
			local time = 200
			local x = parameters.pos[1]
			local y = parameters.pos[2]

			transition.to(self.bg,    { x = x, y = y, time = time })
			transition.to(self.image, { x = x, y = y, time = time, onComplete = function ()
			
				self:setWeapon(defaultWeapon, true)
				self:updateScore(parameters.score, false)
				transition.fadeIn(self.name)
				transition.fadeIn(self.nameBg)
				transition.fadeIn(self.score)
			end })
		end

		function player:animateScore(amount, duration, startValue)
		    local newScore = startValue
		    local passes = (duration / 1000) * display.fps
		    local increment = math.round(lerp(0, amount, 1 / passes))

		    transition.scaleTo(self.score, { xScale = 1.4, yScale = 1.4, time = 400, transition = easing.outQuad, onComplete = function()
 				transition.scaleTo(self.score, { xScale = 1, yScale = 1, time = 400, transition = easing.outQuad })
 			end })
		 
		    local count = 0
		    local function updateText()
		        if (count <= passes and newScore + increment < startValue + amount) then
		            newScore = newScore + increment
		            self.score.text = newScore
		            count = count + 1
		        else
		            Runtime:removeEventListener("enterFrame", updateText)
		            self.score.text = amount + startValue
		        end
		    end
		 
		    Runtime:addEventListener("enterFrame", updateText)
		end

		function player:shot()
			local s = emitter.newShotEmitter()
			playersGroup:insert(s)
			s:toBack()
			s.x,s.y = self.image.x, self.image.y
			timer.performWithDelay(700, function ()
				display.remove(s)
			end)

			if parameters.id == exports.myUserId then
				local hurt = display.newImageRect('assets/images/hurt.png', screenW, screenH)
				hurt.alpha = 0
				hurt.anchorX, hurt.anchorY = 0, 0
				transition.fadeIn(hurt, { time=200, onComplete=function ()
					transition.fadeOut(hurt, { time=200, onComplete=function ()
						display.remove(hurt)
					end})
				end})
			end
		end

		function player:expulse()
			log('expulse (' .. self.deltaX .. ', ' .. self.deltaY .. ')')
			self.image:applyForce(-self.deltaX, -self.deltaY, self.image.x, self.image.y)
		end

		function player:show()

			miniMap:insert(playerMiniImage)

			playersGroup:insert(self.bonusEnabled)
			playersGroup:insert(self.bg)
			playersGroup:insert(self.nameBg)
			playersGroup:insert(self.name)
			playersGroup:insert(self.score)

			scaleUp(self.image)
			scaleUp(self.bg)
			
			if parameters.id == exports.myUserId then
				camera:add(self.image, 1, true)
			else
				playersGroup:insert(self.image)
			end
		end

		function player:hide()
			if self.id == exports.myUserId then
				camera:remove(self.image)
				camera:cancel()
			else
				display.remove(self.image)
			end

			display.remove(self.weapon.image)
			display.remove(self.bonusEnabled)
			display.remove(self.miniImage)
			display.remove(self.bg)
			display.remove(self.nameBg)
			display.remove(self.name)
			display.remove(self.score)
		end

		function player:alpha(alpha)
			playersGroup.alpha = alpha
		end

		function player:loadAvatar(url)

			local placeholderIndex = naiveHash(self.id) % 5
			local placeholderFill = { type='image', filename='assets/images/user_placeholder_sq_' .. placeholderIndex .. '.png' }
			
			if url and url ~= 'http://no' then
				self.image.alpha = 0
				network.download(url, 'get', function (event) 
					self.image.fill = { type='image', filename=event.response.filename, baseDir=event.response.baseDirectory }
					transition.fadeIn(self.image, { time=250 })
				end, crypto.digest(crypto.sha1, url), system.TemporaryDirectory)

			else
				self.image.fill = placeholderFill
			end
		end

		function player:setDeltaMove(deltaX, deltaY)
			self.deltaX = deltaX
			self.deltaY = deltaY
		end

		function player:move(x, y)
			local offset = 0

			if self.weapon and self.weapon.weapon then
				offset = self.weapon.weapon.playerInfosOffset
				self.weapon.image.x, self.weapon.image.y = x, y
			end

			self.image.x, self.image.y = x, y
			self.bg.x, self.bg.y = x, y
			self.name.x, self.name.y = x, y + offset
			self.nameBg.x, self.nameBg.y = x, y + offset
			self.score.x, self.score.y = x, y - offset
			self.miniImage.x, self.miniImage.y = miniMap:convert(x, y)
			self.bonusEnabled.x, self.bonusEnabled.y = x, y
		end

		function player:die()

			self.score.text = ''

			sounds.playDie()
			
			local eA = emitter.newExplosionAEmitter()
			eA.x,eA.y = self.image.x, self.image.y

			local eB = emitter.newExplosionBEmitter()
			eB.x,eB.y = self.image.x, self.image.y

			playersGroup:insert(eA)
			playersGroup:insert(eB)
			eA:toBack()
			eB:toBack()

			scaleDown(self.image)
			scaleDown(self.bg)
			scaleDown(self.name)
			scaleDown(self.nameBg)
			scaleDown(self.score)
			scaleDown(self.miniImage)
			scaleDown(self.bonusEnabled)
			scaleDown(self.weapon.image)

			timer.performWithDelay(2000, function ()
				display.remove(eA)
				display.remove(eB)
			end)
		end

		function player:setWeapon(newWeapon, animated)
			if self.image and self.image.x then
				display.remove(self.weapon.image)
				local playerWeapon = { image = display.newImageRect(newWeapon.skin.image, newWeapon.skin.width, newWeapon.skin.height), weapon = newWeapon }
				self.weapon = playerWeapon
				playersGroup:insert(1, self.weapon.image)
				self:move(self.image.x, self.image.y)
				if animated then
					scaleUp(self.weapon.image)
				end
			end
		end

		function player:displayBonusEnabled(shouldDisplay)
			if shouldDisplay then
				transition.fadeIn(self.bonusEnabled, { time=300, transition = easing.outQuad })
			else
				transition.fadeOut(self.bonusEnabled, { time=300, transition = easing.outQuad })
			end
		end

		player:move(parameters.pos[1], parameters.pos[2])

		players[parameters.id] = player
		broadcastStatuses()

		if parameters.id == exports.myUserId then
			exports.myPlayer = player
			bodiesLibrary.addMyPlayer(player.image)
		else

			local function playerCollision()
    			local myPlayer = exports.myPlayer
    			if myPlayer then
    				local collisionPoints = -1
					gamemaster.collisionPlayer(player.id, collisionPoints, myPlayer:scoreByAddingPoints(collisionPoints, false)) -- pts is negative here
					myPlayer:expulse()
    			end
    			
			end
			bodiesLibrary.addOtherPlayer(player.image, playerCollision)

		end

		if peer then
			player:loadAvatar(peer.picture)
		else
			player:loadAvatar()
		end

		player:show()
	end

	return players[parameters.id]
end

exports.removePeer = function (peerId)
	peers[peerId] = nil
end

exports.removePlayer = function (playerId, camera)

	isDead[playerId] = true

	local player = players[playerId]
	if player then

		players[playerId] = nil

		if camera:layer(1) and camera:layer(1)[1] == player.image then
			camera:remove(player.image)
			camera:cancel()
		end

		local remainingPlayer = nil
		local nbRemainingPlayers = 0
		for k,v in pairs(players) do
			remainingPlayer = v
			nbRemainingPlayers = nbRemainingPlayers + 1
		end

		if nbRemainingPlayers > 1 then
			exports.followBestRemainingPlayer(camera)

			if exports.myUserId == playerId then
				texts.showYouLost()
			else
				texts.showSomeoneLost(player.display_name)
			end

		elseif nbRemainingPlayers == 1 then
			if player.scoreValue == 0 then -- If the other player actually died
				if exports.myUserId == remainingPlayer.id then
					Runtime:dispatchEvent({ name='coronaView', event='saveScore', score=remainingPlayer.scoreValue })
					texts.showYouWon()
				else
					texts.showSomeoneWon(remainingPlayer.display_name)
				end
			else
				texts.showGameOver()
			end
		end

		player:hide()
	end

	broadcastStatuses()
end

exports.followBestRemainingPlayer = function (camera)
	
	local bestRemainingPlayer
	for k,v in pairs(players) do
		if not bestRemainingPlayer or v.scoreValue > bestRemainingPlayer.scoreValue then
			bestRemainingPlayer = v
		end
	end

	if bestRemainingPlayer then
		camera:add(bestRemainingPlayer.image, 1, true)
	end
end

return exports
