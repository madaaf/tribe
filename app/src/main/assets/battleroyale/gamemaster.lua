local listeners = {}

local json = require 'json'

local CMD_SEPARATOR 			= '#'
local CMD_SEND_POSITION 		= 'p:'
local CMD_SEND_BULLET 			= 'b:'
local CMD_SEND_HIT_BY_BULLET	= 'h:'
local CMD_SEND_COLLISION_PLAYER	= 'c:'
local CMD_SEND_SCORE			= 's:'
local CMD_SEND_UTILITY			= 'u:'
local CMD_SEND_WEAPON			= 'w:'
local CMD_SEND_GET_BONUS 		= 'g:'

local function receive(data)

	-- Commands
	if data:starts('&&') then
		local d = data:sub(3)

		-- position
		for cmd in string.gmatch(d, '([^' .. CMD_SEPARATOR .. ']+)') do

    		if cmd:starts(CMD_SEND_POSITION) then
				local event = {}
				event.userId, event.x, event.y = cmd:match(CMD_SEND_POSITION .. '([A-Za-z0-9\-_]+),([0-9.\-]+),([0-9.\-]+)')
				listeners['onPositionEvent'](event)

			elseif cmd:starts(CMD_SEND_BULLET) then
				log('receive - ' .. cmd)
				local event = {}
				event.userId, event.id, event.x, event.y, event.angle, event.speed, event.pts, event.weaponSubtype = cmd:match(CMD_SEND_BULLET .. '([A-Za-z0-9\-_]+),([0-9.\-]+),([0-9.\-]+),([0-9.\-]+),([0-9.\-]+),([0-9.\-]+),([0-9.\-]+),([a-z0-9]+)')
				listeners['onAddBulletEvent'](event)

			elseif cmd:starts(CMD_SEND_HIT_BY_BULLET) then
				log('receive - ' .. cmd)
				local event = {}
				event.userId, event.id = cmd:match(CMD_SEND_HIT_BY_BULLET .. '([A-Za-z0-9\-_]+),([0-9.\-]+)')
				listeners['onRemoveBulletEvent'](event)

			elseif cmd:starts(CMD_SEND_SCORE) then
				local event = {}
				event.userId, event.pts, event.score = cmd:match(CMD_SEND_SCORE .. '([A-Za-z0-9\-_]+),([0-9.\-]+),([0-9.\-]+)')
				listeners['onScoreEvent'](event)

			elseif cmd:starts(CMD_SEND_UTILITY) then
				log('receive - ' .. cmd)
				local event = { type = 'utility' }
				event.id, event.subtype, event.x, event.y = cmd:match(CMD_SEND_UTILITY .. '([A-Za-z0-9\-_]+),([A-Za-z0-9\-_]+),([0-9.\-]+),([0-9.\-]+)')
				listeners['onBonusEvent'](event)

			elseif cmd:starts(CMD_SEND_WEAPON) then
				log('receive - ' .. cmd)
				local event = { type = 'weapon' }
				event.id, event.subtype, event.x, event.y = cmd:match(CMD_SEND_WEAPON .. '([A-Za-z0-9\-_]+),([A-Za-z0-9\-_]+),([0-9.\-]+),([0-9.\-]+)')
				listeners['onBonusEvent'](event)

			elseif cmd:starts(CMD_SEND_GET_BONUS) then
				log('receive - ' .. cmd)
				local event = {}
				event.userId, event.id = cmd:match(CMD_SEND_GET_BONUS .. '([A-Za-z0-9\-_]+),([A-Za-z0-9\-_]+)')
				listeners['onGetBonusEvent'](event)
			end
		end

	-- Session event from the server
	elseif data:starts('??') then
		local event = { session=json.decode(data, 3) }

		log('receive - ' .. data)

		listeners['onSessionEvent'](event)

	-- Game event from the server
	elseif data:starts('**') then
		local n,d = data:match('**([A-Z_]+)**(.*)')
		
		log('receive - ' .. data)

		if n then
			local event = { name=n }
			if d then
				event.payload = json.decode(d)
			end

			listeners['onGameEvent'](event)
		end

	end
end

local dispatch
if isSimulator then
	local mock = require 'mock'
	dispatch = function (data)
		mock.dispatch(data, receive)
	end
else
	dispatch = function (data)
		Runtime:dispatchEvent({ name='coronaView', event='gameMaster', string=data })
	end
end

local exports = {}

local extraCmds = ''

exports.addCmd = function(cmd)
	extraCmds = extraCmds .. CMD_SEPARATOR .. cmd
end

exports.addBullet = function(id, x, y, angle, weapon)
	exports.addCmd(CMD_SEND_BULLET .. id .. ',' .. x .. ',' .. y .. ',' .. angle .. ',' .. weapon.speed .. ',' .. weapon.pts .. ',' .. weapon.subtype)
end

exports.hitByBullet = function(id, pts, score)
	dispatch('&' .. CMD_SEND_HIT_BY_BULLET .. id .. CMD_SEPARATOR .. CMD_SEND_SCORE .. pts .. ',' .. score)
end

exports.collisionPlayer = function(id, pts, score)
	dispatch('&' .. CMD_SEND_COLLISION_PLAYER .. id .. CMD_SEPARATOR .. CMD_SEND_SCORE .. pts .. ',' .. score)
end

exports.getBonusUtility = function(id, pts, score)
	exports.addCmd(CMD_SEND_GET_BONUS .. id .. CMD_SEPARATOR .. CMD_SEND_SCORE .. pts .. ',' .. score)
end

exports.getBonusWeapon = function(id)
	exports.addCmd(CMD_SEND_GET_BONUS .. id)
end

-- IN and OUT Broadcast

exports.setEventListener = function (name, callback)
	listeners[name] = callback
end

exports.broadcastPosition = function (positionX, positionY)
	dispatch('&' .. CMD_SEND_POSITION .. positionX .. ',' .. positionY .. extraCmds)
	extraCmds = ''
end

exports.start = function ()

	Runtime:addEventListener('gameMaster', function (event)
		if event.string then
			receive(event.string)
		end
	end)

	dispatch('?')
end

exports.joinPlayers = function ()
	dispatch('+')
end

exports.leavePlayers = function ()
	dispatch('-')
end

return exports
