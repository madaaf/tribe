---------------------------------------------------------------------------------
-- Modules

local composer = require 'composer'

---------------------------------------------------------------------------------
-- Global

if system.getInfo('environment') == 'simulator' then
	isSimulator = true
else
	isSimulator = false
end

if system.getInfo('platform') == 'ios' then
	safeScreenOriginY = display.safeScreenOriginY
else
	safeScreenOriginY = 0
end

function log(string)
	print('🍺 - Corona - ' .. string)
end

log(system.getInfo('appVersionString'))

---------------------------------------------------------------------------------

composer.gotoScene("battle")
