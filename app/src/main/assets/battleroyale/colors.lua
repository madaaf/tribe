local all_colors = {
	white = {
		image     = 'assets/images/player_white.png',
		miniImage = 'assets/images/mini_player_white.png',
		rgba      = { 1, 1, 1, 1 }
	},
	pink = {
		image     = 'assets/images/player_pink.png',
		miniImage = 'assets/images/mini_player_pink.png',
		rgba      = { 250/255, 127/255, 217/255, 1 }
	},
	cyan = {
		image     = 'assets/images/player_cyan.png',
		miniImage = 'assets/images/mini_player_cyan.png',
		rgba      = { 92/255, 204/255, 250/255, 1 }
	},
	green = {
		image     = 'assets/images/player_green.png',
		miniImage = 'assets/images/mini_player_green.png',
		rgba      = { 56/255, 243/255, 112/255, 1 }
	},
	yellow = {
		image     = 'assets/images/player_yellow.png',
		miniImage = 'assets/images/mini_player_yellow.png',
		rgba      = { 254/255, 190/255, 0/255, 1 }
	},
	orange = {
		image     = 'assets/images/player_orange.png',
		miniImage = 'assets/images/mini_player_orange.png',
		rgba      = { 255/255, 121/255, 2/255, 1 }
	},
	red = {
		image     = 'assets/images/player_red.png',
		miniImage = 'assets/images/mini_player_red.png',
		rgba      = { 250/255, 92/255, 92/255, 1 }
	},
	purple = {
		image     = 'assets/images/player_purple.png',
		miniImage = 'assets/images/mini_player_purple.png',
		rgba      = { 127/255, 71/255, 222/255, 1 }
	},
	blue = {
		image     = 'assets/images/player_blue.png',
		miniImage = 'assets/images/mini_player_blue.png',
		rgba      = { 0/255, 94/255, 255/255, 1 }
	}
}

return function(code)

	if all_colors[code] then
		return all_colors[code]
	end

	return all_colors.pink
end
