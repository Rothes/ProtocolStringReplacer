package io.github.rothes.protocolstringreplacer.command.subcommands

import io.github.rothes.protocolstringreplacer.PsrLocalization
import io.github.rothes.protocolstringreplacer.api.user.PsrUser
import io.github.rothes.protocolstringreplacer.command.SubCommand
import io.github.rothes.protocolstringreplacer.plugin

class About: SubCommand("about", "protocolstringreplacer.command.about", PsrLocalization.getLocaledMessage("Sender.Commands.About.Description")) {

    override fun onExecute(user: PsrUser, args: Array<out String>) {
        user.msg("Header")
        user.msg("Plugin")
        user.msg("Version", plugin.description.version)
        user.msg("Author", "Rothes")
        user.msg("Donate", "https://ko-fi.com/rothes")
        user.msg("GitHub", "https://github.com/Rothes/ProtocolStringReplacer")
        user.msg("To-Say")
        user.msg("Footer")
    }

    override fun onTab(user: PsrUser, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun sendHelp(user: PsrUser) {
    }

    private fun PsrUser.msg(key: String, append: String = "", vararg rep: String) {
        this.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.About.$key", *rep) + append)
    }

}