local composer = require "composer"

-- Global - Hackish way to detect the simulator...
isSimulator = false -- system.getInfo("build") == '2017.3184'

function log(string)
	print('🍺 - Corona - ' .. string)
end

---------------------------------------------------------------------------------

composer.gotoScene("level")
