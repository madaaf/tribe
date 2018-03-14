local strings = {
	default = {
		revive_title = 'Second chance!',
		revive_message = 'Notify your Facebook friends to\nkeep playing at this score!',
		revive_facebook_button = 'NOTIFY & CONTINUE',
		revive_notifying = 'NOTIFYING...',
		revive_skip_button = 'SKIP CHANCE'
	},
	fr = {
		revive_title = 'Seconde chance!',
		revive_message = 'Notifie tes amis Facebook \net reprend le jeu à ton score courant!',
		revive_facebook_button = 'NOTIFIER ET CONTINUER',
		revive_notifying = 'NOTIFICATION EN COURS...',
		revive_skip_button = 'PASSER SA CHANCE'
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
