local strings = {
	default = {
		summary_subtitle 			= 'BEST: ',
		summary_play_again_button 	= 'PLAY AGAIN IN ',
		summary_leaderboard_button 	= 'SHOW LEADERBOARD'
	},
	fr = {
		summary_subtitle 			= 'MEILLEUR: ',
		summary_play_again_button 	= 'REJOUER DANS ',
		summary_leaderboard_button 	= 'LEADERBOARD'
	}
}

local locale = 'default'
if system.getPreference("locale", "language") == 'fr' then
	locale = 'fr'
end

return function (key)
	local value = strings[locale][key]
	if value then
		return value
	else
		return key
	end
end
