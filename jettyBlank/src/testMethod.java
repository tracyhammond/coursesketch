	/**
	 * Sets up a Jetty embedded server. Uses HTTPS over port 12102 and a key certificate.
	 * @throws UnknownHostException 
	 */
	public static void startServer() throws UnknownHostException{
		String message = 
		"\n"+
		"--*---*--***---***--*--*----*----*--*--***--*--*--\n"+
		"--**-**--**---*-----****---***---**-*---*----**---\n"+
		"--*-*-*--***---***--*--*--*---*--*-**--***--*--*--\n";
		System.out.println(message);
		//SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.DEBUG,LogLevel.ERROR);

		TScope.get();
		boolean c = false;


		try {
			hostname = InetAddress.getLocalHost().getHostName();
			hostname = hostname.split("\\.")[0];
		} catch (UnknownHostException e1) {
		}

		PersistenceManager.startDataSources();
		printStartupText();
		try {
			try {
				addDefaults();

				System.setProperty("java.awt.headless", "true");
				//New Jetty Server
				if(MechanixServer.config.serverSecure)
					server = new Server();
				else
					server = new Server(MechanixServer.config.serverPort);



				if(MechanixServer.config.serverSecure){

					//Configure SSL
					SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
					ssl_connector.setPort(MechanixServer.config.serverPort);
					SslContextFactory cf = ssl_connector.getSslContextFactory();

					File keystore = new File("/share/Download/nss324-o.keystore");
					if(keystore.exists()){
						//Use the real certificate
						System.out.println("Loaded real keystore");
						cf.setKeyStorePath(keystore.getPath());
						cf.setTrustStore(keystore.getPath());
						cf.setTrustStorePassword("george");
						//cf.setCertAlias("nss324-o");
						//cf.checkKeyStore();

					}
					else{
						//Use the Civil Sketch keystore. Not secure, but it will have to do.
						System.out.println("Loaded temporary keystore");
						cf.setKeyStorePath("civilKeystore");
					}

					cf.setKeyStorePassword("george");

					server.setConnectors(new Connector[]{ssl_connector});
				}


				//Configure Resource (Html etc) handler
				ContextHandler resourceHandler = new ContextHandler();
				try {
					resourceHandler.setBaseResource(Resource.newResource(new File("server/htdocs")));
					resourceHandler.setHandler(new ResourceHandler());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				//Configure Session persistence

				logger.info("Registering Session Storage for host: "+hostname);

				JDBCSessionIdManager sessionIdManager = new JDBCSessionIdManager(server);
				sessionIdManager.setDatasourceName(MechanixServer.config.datastoreJndi);
				sessionIdManager.setWorkerName(hostname);
				sessionIdManager.setScavengeInterval(60);
				server.setSessionIdManager(sessionIdManager);

				JDBCSessionManager jdbcMgr = new JDBCSessionManager();
				jdbcMgr.setSessionIdManager(server.getSessionIdManager());


				//Configure Mechanix Request Handler
				stats = new StatisticsHandler();

				ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
				servletHandler.setContextPath("/mechanix");
				servletHandler.addServlet(new ServletHolder(new MechanixServlet()),"/");
				servletHandler.addServlet(new ServletHolder(new SubmissionRenderServlet()), "/submission/*");
				servletHandler.setSessionHandler(new SessionHandler(jdbcMgr));

				stats.setHandler(servletHandler);
				HandlerList handlers = new HandlerList();
				handlers.setHandlers(new Handler[]{stats,resourceHandler});

				server.setHandler(handlers);


				try {
					if(config.serverStats)
						MechanixServer.getStatisticsThread().start();
					if(config.rebuildEnabled)
						MechanixServer.getRebuildThread().start();
					server.start();
					server.join();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}   finally {
		}
	}
