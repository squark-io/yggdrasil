package io.squark.yggdrasil.core.context

import java.util.*
import javax.naming.Binding
import javax.naming.CompoundName
import javax.naming.Context
import javax.naming.Name
import javax.naming.NameAlreadyBoundException
import javax.naming.NameClassPair
import javax.naming.NameNotFoundException
import javax.naming.NameParser
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.OperationNotSupportedException


/**
 * yggdrasil
 *
 *
 * Created by Erik Håkansson on 2017-04-01.
 * Copyright 2017
 */
open class YggdrasilContext(val name : String, val parent : Context? = null) : Context {

  val bindings : MutableMap<String, Binding> by lazy {
    mutableMapOf<String, Binding>()
  }
  val subContexts : MutableMap<String, YggdrasilContext> by lazy {
    mutableMapOf<String, YggdrasilContext>()
  }

  private companion object {
    @JvmStatic val environmentTable: Hashtable<String, Any> by lazy {
      Hashtable<String, Any>()
    }
    @JvmStatic private val syntax = Properties()
    init {
      syntax.put("jndi.syntax.direction", "left_to_right")
      syntax.put("jndi.syntax.separator", "/")
      syntax.put("jndi.syntax.separator2", ":")
      syntax.put("jndi.syntax.ignorecase", "true")
      syntax.put("jndi.syntax.escape", "\\")
      syntax.put("jndi.syntax.beginquote", "\"")
      syntax.put("jndi.syntax.trimblanks", "false")
    }
  }

  override fun listBindings(name: Name): NamingEnumeration<Binding> {
    return YggdrasilNamingEnumeration(getSubContext(name).bindings.values)
  }

  override fun listBindings(name: String): NamingEnumeration<Binding> {
    return listBindings(CompoundName(name, syntax))
  }

  override fun destroySubcontext(name: Name) {
    val subContext = getSubContext(name)
    val nameAsString = subContext.name
    val parent = subContext.parent
    if (parent == this) {
      subContexts.remove(nameAsString)
    } else {
      subContext.destroySubcontext(nameAsString)
    }
  }

  override fun destroySubcontext(name: String?) {
    destroySubcontext(CompoundName(name, syntax))
  }

  override fun list(name: Name): NamingEnumeration<NameClassPair> {
    return YggdrasilNamingEnumeration(getSubContext(name).bindings.values)
  }

  override fun list(name: String?): NamingEnumeration<NameClassPair> {
    return list(CompoundName(name, syntax))
  }

  override fun getNameInNamespace(): String {
    throw OperationNotSupportedException()
  }

  override fun rebind(name: Name, obj: Any) {
    var subContext = getSubContext(name.getPrefix(name.size() - 1))
    val nameAsString = name[name.size() - 1]
    subContext.bindings[nameAsString] ?: throw NameNotFoundException("$subContext/$nameAsString")
    subContext.bindings.replace(nameAsString, Binding(nameAsString, obj.javaClass.name, obj))
  }

  override fun rebind(name: String?, obj: Any) {
    rebind(CompoundName(name, syntax), obj)
  }

  override fun unbind(name: Name) {
    var subContext = getSubContext(name.getPrefix(name.size() - 1))
    val nameAsString = name[name.size() - 1]
    subContext.bindings[nameAsString] ?: throw NameNotFoundException("$subContext/$nameAsString")
    subContext.bindings.remove(nameAsString)
  }

  override fun unbind(name: String?) {
    unbind(CompoundName(name, syntax))
  }

  override fun composeName(name: Name, prefix: Name): Name {
    if (!prefix.isEmpty) throw NamingException("Prefix must be empty")
    return name
  }

  override fun composeName(name: String?, prefix: String): String {
    return composeName(CompoundName(name, syntax), CompoundName(prefix, syntax)).toString()
  }

  override fun getEnvironment(): Hashtable<*, *> {
    return environmentTable
  }

  override fun bind(name: Name, obj: Any) {
    val nameAsString = name[name.size() - 1]
    val binding = Binding(nameAsString, obj.javaClass.name, obj)
    bind(name, binding)
  }

  override fun bind(name: String, obj: Any) {
    bind(CompoundName(name, syntax), obj)
  }

  fun bind(name: Name, binding: Binding) {
    val subContext = getSubContext(name.getPrefix(name.size() - 1))
    val nameAsString = name[name.size() - 1]
    if (subContext.bindings.containsKey(nameAsString)) throw NameAlreadyBoundException("$subContext/$nameAsString")
    subContext.bindings.put(nameAsString, binding)
  }

  fun bind(name: String, binding: Binding) {
    bind(CompoundName(name, syntax), binding)
  }

  override fun removeFromEnvironment(propName: String): Any? {
    return environmentTable[propName]
  }

  override fun createSubcontext(name: Name): Context {
    val subContext = getSubContext(name.getPrefix(name.size() - 1))
    val nameAsString = name[name.size() - 1]
    if (subContext.subContexts.containsKey(nameAsString)) throw NameAlreadyBoundException("$subContext/$nameAsString")
    val newContext = YggdrasilContext(nameAsString, subContext)
    subContext.subContexts[nameAsString] = newContext
    return newContext
  }

  override fun createSubcontext(name: String): Context {
    return createSubcontext(CompoundName(name, syntax))
  }

  override fun rename(oldName: Name, newName: Name) {
    val oldSubContext = getSubContext(oldName.getPrefix(oldName.size() - 1))
    val oldNameAsString = oldName[oldName.size() - 1]
    if (!oldSubContext.bindings.containsKey(oldNameAsString)) throw NameNotFoundException(oldName.toString())
    val newSubContext = getSubContext(newName.getPrefix(newName.size() - 1))
    val newNameAsString = newName[newName.size() - 1]
    if (newSubContext.bindings.containsKey(newNameAsString)) throw NameAlreadyBoundException(newName.toString())
    val oldBinding : Binding = oldSubContext.bindings[oldNameAsString] as Binding
    newSubContext.bindings[newNameAsString] = Binding(newNameAsString, oldBinding.className, oldBinding.`object`)
    oldSubContext.bindings.remove(oldNameAsString)
  }

  override fun rename(oldName: String, newName: String) {
    rename(CompoundName(oldName, syntax), CompoundName(newName, syntax))
  }

  override fun lookupLink(name: Name): Any {
    return lookup(name)
  }

  override fun lookupLink(name: String): Any {
    return lookup(name)
  }

  override fun addToEnvironment(propName: String, propVal: Any): Any? {
    return environmentTable.set(propName, propVal)
  }

  override fun getNameParser(name: Name): NameParser {
    if (name.isEmpty) {
      return CompoundNameParser()
    }
    return getSubContext(name).getNameParser(CompoundName("", syntax))
  }

  override fun getNameParser(name: String): NameParser {
    return getNameParser(CompoundName(name, syntax))
  }

  override fun close() {
    bindings.clear()
    subContexts.values.forEach { it.close() }
    subContexts.clear()
  }

  override fun lookup(name: Name): Any {
    val binding: Binding?
    if (name.isEmpty) {
      return this
    } else if (name.size() == 1) {
      binding = bindings[name[0]]
      return when (binding) {
        null -> throw NameNotFoundException("$name")
        else -> binding.`object`
      }
    } else {
      val subContext = getSubContext(name.getPrefix(name.size() - 1))
      val nameAsString = name[name.size() - 1]
      binding = subContext.bindings[nameAsString]
      return when (binding) {
        null -> throw NameNotFoundException("$subContext/$nameAsString")
        else -> binding.`object`
      }
    }
  }

  override fun lookup(name: String): Any {
    return lookup(CompoundName(name, syntax))
  }

  private fun getSubContext(name: Name): YggdrasilContext {
    var currentContext : YggdrasilContext? = this
    for (part in name.all) {
      currentContext = currentContext!!.subContexts[part]
      if (currentContext == null) {
        throw NameNotFoundException("$name")
      }
    }
    return currentContext!!
  }

  override fun toString(): String {
    return when {
      parent != null -> "$parent/$name"
      else -> name
    }
  }

  class CompoundNameParser : NameParser {
    override fun parse(name: String?): Name {
      return CompoundName(name, syntax)
    }
  }
}
