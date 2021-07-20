package br.ufes.inf.ngn.televoto.client.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		System.out.println("=> BUNDLE: br.ufes.inf.ngn.televoto.client.manager | CLASS: Activator | METOD: getContext ");//By Ju
		return context;
	}

	
	@SuppressWarnings("resource")
	public void start(BundleContext bundleContext) throws Exception {
		//System.out.println("=> BUNDLE: br.ufes.inf.ngn.televoto.client.manager | CLASS: Activator | METOD: start ");//By Ju
		
		Scanner entrada = new Scanner(System.in);
		int opc;
		
		System.out.println("1) Criar usuários no IMS.");
		System.out.println("2) ....");
		System.out.print("OPÇÃO:  ");
		opc = entrada.nextInt();
		System.out.println("");
		switch (opc) {
			case (1):
				createUsers(bundleContext);
				break;
		}
	}


	public void stop(BundleContext bundleContext) throws Exception {
		System.out.println("=> BUNDLE: br.ufes.inf.ngn.televoto.client.manager | CLASS: Activator | METOD: stop ");//By Ju
		Activator.context = null;
	}
	
	public void createUsers(BundleContext bundleContext) throws Exception {
		//System.out.println("=> BUNDLE: br.ufes.inf.ngn.televoto.client.manager | CLASS: Activator | METOD: createUsers ");//By Ju
		Activator.context = bundleContext;
		java.sql.PreparedStatement st;
		Connection conn = null;
		Object televotoConf[] = new Object[6];
		String query, aux[], server="", user="", password="", dominio="";
		int i = 0, ramal, qtd=0, ramalInicial=0;
						
		System.out.print("Lendo o arquivo de configurações...   ");
		try { 
			FileReader arq = new FileReader("/televotocreateusers.conf");
			BufferedReader lerArq = new BufferedReader(arq); 
			String linha = lerArq.readLine();
			while (linha != null) {
				if (!linha.startsWith("#")) {
					aux = linha.split("=");
					televotoConf[i] = aux[1];
					linha = lerArq.readLine(); 
					i++;
				} else
					linha = lerArq.readLine();
			}
				arq.close();
				server = (String) televotoConf[0];
				user = (String) televotoConf[1];
				password = (String) televotoConf[2];
				qtd = Integer.parseInt( (String) televotoConf[3]);
				ramalInicial = Integer.parseInt( (String) televotoConf[4]);
				dominio = (String) televotoConf[5];
				System.out.print("[OK] \n");
		} catch (IOException e) {
			System.out.print("[FAILED]\n");
			System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage()); 
			System.exit(0);
		} 
		
		System.out.print("Conectado a base de dados...   ");
		try {
			String myDriver = "org.gjt.mm.mysql.Driver";
			String myUrl = "jdbc:mysql://" + server +  "/hss_db";
			Class.forName(myDriver);
			conn = DriverManager.getConnection(myUrl, user, password);
			System.out.print("[OK] \n");
		} catch (Exception e) {
			System.out.print("[FAILED] \n");
			System.err.print("Problemas com o SGDB: ");
			System.err.print(e.getMessage());
		}
		
		System.out.print("Criando IMSs Subscriptions {imsu}...   ");
		try {
			query = "INSERT INTO imsu (id, name, scscf_name, diameter_name, id_capabilities_set, id_preferred_scscf_set) VALUES (?, ?, '', '', '1', '1');";
			st = conn.prepareStatement(query);
			for (ramal = ramalInicial; ramal <(ramalInicial+qtd); ramal++ ) {
				st.setInt(1, ramal);
				st.setInt(2, ramal);
				st.executeUpdate();
			}
			st.close();
			System.out.print("[OK] --> De " + ramalInicial + " à " + (ramal-1) + "\n");
		} catch (Exception e) {
			System.out.print("[FAILED] \n");
			System.err.print("Problemas com o SGDB: ");
			System.err.println(e.getMessage());
		}
		
		System.out.print("Criando \"Private Users Identitys\" {impi}...   ");
		try {
			query = "INSERT INTO hss_db.impi (id, id_imsu, identity, k, auth_scheme, default_auth_scheme, amf, op, sqn, ip, line_identifier, zh_uicc_type, zh_key_life_time, zh_default_auth_scheme) VALUES (?, ?, ?, 0x5034737377307264, '4', '4', 0x0000, 0x00000000000000000000000000000000, '000000000000', '', '', '0', '3600', '1');";
			st = conn.prepareStatement(query);
			for (ramal = ramalInicial; ramal <(ramalInicial+qtd); ramal++ ) {
				st.setInt(1, ramal);
				st.setInt(2, ramal);
				st.setString(3, ramal + "@" + dominio);
				st.executeUpdate();
			}
			st.close();
			System.out.print("[OK] --> De " + ramalInicial + " à " + (ramal-1) + "\n");
		} catch (Exception e) {
			System.out.print("[FAILED] \n");
			System.err.print("Problemas com o SGDB: ");
			System.err.println(e.getMessage());
		}
		
		System.out.print("Criando \"Public Users Identitys\" {impu}...   ");
		try {
			query = "INSERT INTO hss_db.impu (id, identity, type, barring, user_state, id_sp, id_implicit_set, id_charging_info, wildcard_psi, display_name, psi_activation, can_register) VALUES (?, ?, '0', '0', '0', '1', ?, '1', '', '', '0', '1');";
			st = conn.prepareStatement(query);
			for (ramal = ramalInicial; ramal <(ramalInicial+qtd); ramal++ ) {
				st.setInt(1, ramal);
				st.setString(2, "sip:" + ramal + "@" + dominio);
				st.setInt(3, ramal);
				st.executeUpdate();
			}
			st.close();
			System.out.print("[OK] --> De " + ramalInicial + " à " + (ramal-1) + "\n");
		} catch (Exception e) {
			System.out.print("[FAILED] \n");
			System.err.print("Problemas com o SGDB: ");
			System.err.println(e.getMessage());
		}
		
		System.out.print("Relacionando \"Private\" e \"Public\" \"Users Identitys\" {impi_impu}...   ");
		try {
			query = "INSERT INTO hss_db.impi_impu (id, id_impi, id_impu, user_state) VALUES (?, ?, ?, '0');";
			st = conn.prepareStatement(query);
			for (ramal = ramalInicial; ramal <(ramalInicial+qtd); ramal++ ) {
				st.setInt(1, ramal);
				st.setInt(2, ramal);
				st.setInt(3, ramal);
				st.executeUpdate();
			}
			st.close();
			System.out.print("[OK] \n");
		} catch (Exception e) {
			System.out.print("[FAILED] \n");
			System.err.print("Problemas com o SGDB: ");
			System.err.println(e.getMessage());
		}
		
		System.out.print("Relacionando \"Public Users Identitys\" e \"Visited Network\" {impu_visited_network}...   ");
		try {
			query = "INSERT INTO hss_db.impu_visited_network (id, id_impu, id_visited_network) VALUES (?, ?, '1');";
			st = conn.prepareStatement(query);
			for (ramal = ramalInicial; ramal <(ramalInicial+qtd); ramal++ ) {
				st.setInt(1, ramal);
				st.setInt(2, ramal);
				st.executeUpdate();
			}
			st.close();
			System.out.print("[OK] \n");
		} catch (Exception e) {
			System.out.print("[FAILED] \n");
			System.err.print("Problemas com o SGDB: ");
			System.err.println(e.getMessage());
		}
	}

}