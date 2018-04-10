local exports = {}

local weaponList = {
	default = { 
		subtype = 'default',
		speed = 250, 
		name = 'Basic Gun', 
		pts = -1, 
		shootingDelay = 200,
		bulletSize = 10,
		spriteBulletPrefix = 'assets/images/player_',
		skin = { image = 'bonus/weapons/assets/images/skin_default.png', width = 70, height = 70 },
		bulletOffset = 0,
		playerInfosOffset = 50
	},
	m16 = { 
		subtype = 'm16',
		speed = 500, 
		name = 'M16 Assault Rifle', 
		pts = -1, 
		shootingDelay = 50,
		timer = 10000,
		bulletSize = 10,
		spriteBulletPrefix = 'assets/images/player_',
		skin = { image = 'bonus/weapons/assets/images/skin_m16.png', width = 80, height = 80 },
		emoji = { image = 'bonus/weapons/assets/images/emoji_m16.png', width = 65, height = 65 },
		bulletOffset = 10,
		playerInfosOffset = 52
	},
	rocket = { 
		subtype = 'rocket',
		speed = 150, 
		name = 'Rocket Launcher', 
		pts = -10, 
		shootingDelay = 500,
		timer = 10000,
		bulletSize = 28,
		spriteBulletPrefix = 'assets/images/player_',
		skin = { image = 'bonus/weapons/assets/images/skin_rocket.png', width = 75, height = 75 },
		emoji = { image = 'bonus/weapons/assets/images/emoji_rocket.png', width = 65, height = 65 },
		bulletOffset = 10,
		playerInfosOffset = 52
	}, 
	shotgun = { 
		subtype = 'shotgun',
		speed = 150, 
		name = 'Shotgun', 
		pts = -5, 
		shootingDelay = 500,
		timer = 10000,
		bulletSize = 18,
		spriteBulletPrefix = 'assets/images/player_',
		skin = { image = 'bonus/weapons/assets/images/skin_shotgun.png', width = 85, height = 85 },
		emoji = { image = 'bonus/weapons/assets/images/emoji_shotgun.png', width = 65, height = 65 },
		bulletOffset = 10,
		playerInfosOffset = 60
	}
}

----------------------------------------------------------------
-- FUNCTION: CREATE 
----------------------------------------------------------------

exports.defaultWeapon = function()
	local weapon = weaponList['default']
	return weapon
end

exports.randomWeapon = function()
	local keyset = {}
	
	for k in pairs(weaponList) do
		table.insert(keyset, k)
	end

	local weapon = weaponList[keyset[math.random(2, #keyset)]]
	return weapon
end

exports.getWeapon = function(weaponSubtype)
	local weapon = weaponList[weaponSubtype]
	return weapon
end

return exports