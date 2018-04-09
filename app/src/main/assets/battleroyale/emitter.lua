---------------------------------------------------------------------------------
-- Modules

local json = require 'json'

---------------------------------------------------------------------------------
-- Local Functions

local function readParams(path)

	local filePath = system.pathForFile(path)
	local f = io.open(filePath, 'r')
	local emitterData = f:read('*a')
	f:close()

	return json.decode(emitterData)
end

---------------------------------------------------------------------------------
-- Parameters

local exports 	= {}
local params 	= {
	shot       = readParams('assets/particles/shot.json'),
	explosionA = readParams('assets/particles/explosionA.json'),
	explosionB = readParams('assets/particles/explosionB.json'),
	shield     = readParams('assets/particles/shield.json'),
	trail 	   = readParams('assets/particles/trail.json'),
	bonusEnabled = readParams('assets/particles/bonus_enabled.json')
}

---------------------------------------------------------------------------------
-- Export Functions

exports.newShotEmitter = function ()
	return display.newEmitter(params.shot)
end

exports.newBonusEnabledEmitter = function (rgba)

	local coloredParams = params.bonusEnabled
	coloredParams.startColorRed    = rgba[1]
	coloredParams.startColorGreen  = rgba[2]
	coloredParams.startColorBlue   = rgba[3]
	coloredParams.finishColorRed   = 0
	coloredParams.finishColorGreen = 0
	coloredParams.finishColorBlue  = 0

	return display.newEmitter(coloredParams)
end

exports.newExplosionAEmitter = function ()
	return display.newEmitter(params.explosionA)
end

exports.newExplosionBEmitter = function ()
	return display.newEmitter(params.explosionB)
end

exports.newShieldEmitter = function ()
	return display.newEmitter(params.shield)
end

exports.newTrailEmitter = function ()
	return display.newEmitter(params.trail)
end

return exports
