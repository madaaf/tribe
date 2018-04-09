local weaponsLibrary = require('bonus.weapons.weapon')
local utilitiesLibrary = require('bonus.utilities.utility')

local exports = {}

----------------------------------------------------------------
-- FUNCTION: CREATE 
----------------------------------------------------------------

exports.getBonus = function(type, subtype)
	if type == 'weapon' then
		return weaponsLibrary.getWeapon(subtype)
	else 
		return utilitiesLibrary.getUtility(subtype)
	end
end

exports.defaultWeapon = function()
	return weaponsLibrary.defaultWeapon()
end

return exports