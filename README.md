# HAASim
Projeto Simulador de monitoramento remoto de pacientes para plataforma HealthDash Cloud.

Configuração para execução do projeto no eclipse:

<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<launchConfiguration type="org.eclipse.pde.ui.EquinoxLauncher">
    <booleanAttribute key="append.args" value="true"/>
    <booleanAttribute key="automaticAdd" value="false"/>
    <booleanAttribute key="automaticValidate" value="false"/>
    <stringAttribute key="bootstrap" value=""/>
    <stringAttribute key="checked" value="[NONE]"/>
    <booleanAttribute key="clearConfig" value="true"/>
    <stringAttribute key="configLocation" value="${workspace_loc}/.metadata/.plugins/org.eclipse.pde.core/televoto"/>
    <booleanAttribute key="default" value="true"/>
    <booleanAttribute key="default_auto_start" value="true"/>
    <intAttribute key="default_start_level" value="4"/>
    <setAttribute key="deselected_workspace_bundles"/>
    <booleanAttribute key="includeOptional" value="false"/>
    <booleanAttribute key="org.eclipse.jdt.launching.ATTR_ATTR_USE_ARGFILE" value="false"/>
    <booleanAttribute key="org.eclipse.jdt.launching.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES" value="true"/>
    <booleanAttribute key="org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD" value="true"/>
    <stringAttribute key="org.eclipse.jdt.launching.JRE_CONTAINER" value="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-11"/>
    <stringAttribute key="org.eclipse.jdt.launching.PROGRAM_ARGUMENTS" value="-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl} -consoleLog -console"/>
    <stringAttribute key="org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER" value="org.eclipse.pde.ui.workbenchClasspathProvider"/>
    <stringAttribute key="org.eclipse.jdt.launching.VM_ARGUMENTS" value="-Declipse.ignoreApp=true -Dosgi.noShutdown=true"/>
    <stringAttribute key="pde.version" value="3.3"/>
    <setAttribute key="selected_target_bundles">
        <setEntry value="jade.jadeOsgi@default:default"/>
        <setEntry value="javax.servlet@default:default"/>
        <setEntry value="org.apache.commons.math3@default:default"/>
        <setEntry value="org.apache.felix.gogo.command@default:default"/>
        <setEntry value="org.apache.felix.gogo.runtime@default:default"/>
        <setEntry value="org.apache.felix.gogo.shell@default:default"/>
        <setEntry value="org.eclipse.equinox.common@2:true"/>
        <setEntry value="org.eclipse.equinox.console@default:default"/>
        <setEntry value="org.eclipse.equinox.launcher.cocoa.macosx.x86_64@default:false"/>
        <setEntry value="org.eclipse.equinox.launcher@default:default"/>
        <setEntry value="org.eclipse.osgi.compatibility.state@default:false"/>
        <setEntry value="org.eclipse.osgi.services@default:default"/>
        <setEntry value="org.eclipse.osgi.util@default:default"/>
        <setEntry value="org.eclipse.osgi@-1:true"/>
        <setEntry value="org.eclipse.update.configurator@default:default"/>
    </setAttribute>
    <setAttribute key="selected_workspace_bundles">
        <setEntry value="br.ufes.inf.ngn.televoto.client.logic@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.client.service@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.server.as.logic@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.server.as.service@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.server.ras.logic@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.server.ras.service@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.tools@default:false"/>
        <setEntry value="br.ufes.inf.ngn.televoto.web@default:false"/>
    </setAttribute>
    <booleanAttribute key="show_selected_only" value="false"/>
    <booleanAttribute key="tracing" value="false"/>
    <booleanAttribute key="useCustomFeatures" value="false"/>
    <booleanAttribute key="useDefaultConfigArea" value="true"/>
</launchConfiguration>
