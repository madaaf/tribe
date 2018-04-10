local strings = {
	default = {
		training_mode = 'TRAINING MODE',
		viewer_mode = 'VIEWER MODE',
		waiting_instructions = 'It will start when\na player joins',
		pending_instructions = 'It will restart when\na player wins',
		starting_instructions = 'Next battle\nstarts in %s',
		game_over = 'Game interrupted',
		you_lost = 'You lost!',
		you_won = 'You won!',
		someone_lost = '%s lost!',
		someone_won = '%s won!',
		baseline = 'Be the last to survive!',
		shoot = 'shoot',
		move = 'move',
		connection_issue = 'Connection issue'
	},
	fr = {
		training_mode = 'MODE ENTRAINEMENT',
		viewer_mode = 'MODE SPECTATEUR',
		waiting_instructions = 'La partie démarre\nquand un joueur arrive',
		pending_instructions = 'La partie va redémarrer\ndès qu’un joueur gagne',
		starting_instructions = 'La partie démarre\ndans %s',
		game_over = 'Partie interrompue !',
		you_lost = 'Tu as perdu !',
		you_won = 'Tu as gagné !',
		someone_lost = '%s a perdu !',
		someone_won = '%s a gagné !',
		baseline = 'Sois le dernier\nà survivre !',
		shoot = 'tirer',
		move = 'bouger',
		connection_issue = 'Problème de connexion'
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
