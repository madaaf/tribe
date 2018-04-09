local exports = {}

local utilityList = {
	potion_small = { 
		subtype = 'potion_small',
		name = 'Small Health Potion',
		property_change = 'points',
		value = 20,
		emoji = { image = 'bonus/utilities/assets/images/potion_small.png', width = 66, height = 65 },
		timer = 1000
	},
	potion_large = { 
		subtype = 'potion_large',
		name = 'Large Health Potion',
		property_change = 'points',
		value = 50,
		emoji = { image = 'bonus/utilities/assets/images/potion_large.png', width = 66, height = 65 },
		timer = 1000
	},
	speed_up = {
		subtype = 'speed_up',
		name = 'Speed Up',
		property_change = 'speed',
		value = 1.6,
		emoji = { image = 'bonus/utilities/assets/images/emoji_speed_up.png', width = 26, height = 33.5 },
		timer = 5000
	}
}

----------------------------------------------------------------
-- FUNCTION: CREATE 
----------------------------------------------------------------

exports.getUtility = function(utilitySubtype)
	local utility = utilityList[utilitySubtype]
	return utility
end

return exports