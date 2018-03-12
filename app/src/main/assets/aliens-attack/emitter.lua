local json = require 'json'

local function readParams(path)

	local filePath = system.pathForFile(path)
	local f = io.open(filePath, 'r')
	local emitterData = f:read('*a')
	f:close()

	return json.decode(emitterData)
end

local params = {
	alien_falling = readParams('assets/particles/alien_falling.json'),
	fog 		  = readParams('assets/particles/fog.json'),
	bonus 		  = readParams('assets/particles/bonus.json'),
	boom 		  = readParams('assets/particles/boom.json'),
	stars 		  = readParams('assets/particles/stars.json')
}
local exports = {}

exports.newAlienFallingEmitter = function ()
	return display.newEmitter(params.alien_falling)
end

exports.newFogEmitter = function ()
	return display.newEmitter(params.fog)
end

exports.newStarsEmitter = function ()
	return display.newEmitter(params.stars)
end

exports.newBonusEmitter = function ()
	return display.newEmitter(params.bonus)
end

exports.newBoomEmitter = function ()
	return display.newEmitter(params.boom)
end

return exports
