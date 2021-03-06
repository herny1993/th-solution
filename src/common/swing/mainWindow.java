package common.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import common.FiltroArchivoEstructura;
import common.FiltroArchivoTexto;
import common.Messages;
import common.WinRegistry;
import common.estructura.Almacenamiento;
import common.estructura.Elemento;
// import org.apache.log4j.Logger;

public class mainWindow {
	
	// Ruta del archivo de ayuda
	private static final String DIR_MANUAL = "doc\\manual.pdf"; // Windows //$NON-NLS-1$
	//private static final String DIR_MANUAL = "./doc/manual.pdf"; // Linux
	
	// Ruta de salida estandar
	private static final String DIR_SALIDA = "logs.log";  // Windows //$NON-NLS-1$
	//private static final String DIR_SALIDA = "./logs.log"; // Linux
	
	// Ruta de salida de errores
	private static final String DIR_ERR = "reports.log";  // Windows //$NON-NLS-1$
	//private static final String DIR_ERR = "./logs.log"; // Linux

	// Directorio de instalacion
	public static String INSTALL_PATH = ""; // Windows //$NON-NLS-1$
	
	// Directorio de usuario
	public static String USER_HOME = ""; // Windows //$NON-NLS-1$
	
	// Directorio de aplicacion
	public static String APP_HOME = ""; // Windows //$NON-NLS-1$

	// --------- Panel Derecho - Begin --------- //
	
	private JPanel panelDerecho;
	
	// Informacion
	private JLabel lblInformacion;
	private JTextPane txtInformacion;
	private Box verticalBox;

	// Acciones
	private JLabel lblAcciones;
	private JTextField txtElementos;
	private Box boxAcciones;
	private JButton btnInsertar;
	private JButton btnEliminar;	
	
	// Capturas
	private JLabel lblCapturas;
	private Box boxCapturas;
	private JButton btnPrimero;
	private JButton btnSiguiente;
	private JButton btnAnterior;
	private JButton btnUltimo;

	// --------- Panel Derecho - End --------- //
	
	// --------- Menu - Begin --------- //
	
	private JMenuBar menuBar;

	private JMenu mnArchivo;
	private JMenuItem mntmNuevaEstructura;
	private JMenuItem mntmAbrir;
	private JMenuItem mntmGuardar;
	private JMenuItem mntmGuardarComo;
	private JMenuItem mntmCerrarEstructura;
	private JMenuItem mntmSalir;

	private JMenu mnBuscar;
	private JMenuItem mntmRealizarBusqueda;
	private JMenuItem mntmCargarAcciones;
	private JMenuItem mntmGenerarElementos;

	private JMenu mnIdioma;
	private ButtonGroup groupIdioma;
	private JRadioButtonMenuItem rdbtnmntmEspanolargentina;
//	private JRadioButtonMenuItem rdbtnmntmInglesUs;

	private JMenu mnAyuda;
	private JMenuItem mntmAyuda;
	private JMenuItem mntmAcercaDe;

	// --------- Menu - End --------- //

	// --------- Consola - Begin --------- //
	
	private JLabel lblConsola;
	private final JButton btnBorrar = new JButton(""); //$NON-NLS-1$
	private final JButton btnMax = new JButton(""); //$NON-NLS-1$
	private final JButton btnMin = new JButton(""); //$NON-NLS-1$

	private JTextPane txtConsola;
	private JScrollPane scrollConsola;
	private final JPanel panelConsola = new JPanel();
	
	// --------- Consola - End --------- //
	
	public JFrame frmAplicacion;
	public Archivo archivo;
	public JTabbedPane tabsArchivos;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		final String[] argumentos = args;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					// Crear aplicacion
					mainWindow window = new mainWindow();
					window.frmAplicacion.setVisible(true);
					
					// Procesar Argumentos
					for (String s : argumentos){
						System.out.println("Abrir " + s); //$NON-NLS-1$
						window.abrirEstructura(new File(s));
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public mainWindow() {
		
		// Configuracion
		configurePath();
		configureOutput();
		
		// Inicializacion
		initWindow();
		initMenu();		
		initPanelDerecho();
		initConsola();
		
		// Managers
		InfoManager.getInstance(txtInformacion); // Asignamos el InfoManager
		ConsolaManager.getInstance(txtConsola, scrollConsola); // Asignamos el ConsolaManager
		
		// Generar texto segun lenguaje seleccionado
		generateMessages();
		
		// Actualizar imagenes
		actualizarImagen();
		
	}


	/***********************************************************************************
	 *                                                                                 *
	 *                              TODO CONFIGURACIONES                               *
	 *                                                                                 *
	 ***********************************************************************************/
	
	/** 
	 * Configurar los siguientes directorios:
	 *    . USER_HOME
	 *    . APP_HOME (guardar logs)
	 *    . INSTALL_PATH (acceder a ayuda, dot)
	 * Para ello, accede al registro:
	 *    . HKEY_LOCAL_MACHINE/SOFTWARE/THSolution, InstallPath (32 bits)
	 *    . HKEY_LOCAL_MACHINE/SOFTWARE/Wow6432Node/THSolution, InstallPath (64 bits)
	 */
	private void configurePath() {
		// Configurar carpeta de usuario
		USER_HOME = System.getProperty("user.home"); //$NON-NLS-1$
		
		// Configurar carpeta de aplicacion
		File appDir = new File(USER_HOME + "\\TH Solution\\"); //$NON-NLS-1$

		if (!appDir.exists() || !appDir.isDirectory()) {
			boolean result = appDir.mkdir();

			if(result) {    
				System.out.println("DIRECTORIO creado");   //$NON-NLS-1$
			}
		}
		APP_HOME = appDir.getAbsolutePath() + "\\"; //$NON-NLS-1$
		
		// Configurar carpeta de instalacion
		String value;
		try {
			value = WinRegistry.readString (
				    WinRegistry.HKEY_LOCAL_MACHINE,                             //HKEY
				   "SOFTWARE\\THSolution",                                      //Key (32 bits) //$NON-NLS-1$
				   "InstallPath");                                              //ValueName //$NON-NLS-1$
			if (value == null){
				value = WinRegistry.readString (
					    WinRegistry.HKEY_LOCAL_MACHINE,                         //HKEY
					   "SOFTWARE\\Wow6432Node\\THSolution",                     //Key (64 bits) //$NON-NLS-1$
					   "InstallPath");                                          //ValueName				 //$NON-NLS-1$
			}
			
			if(value == null){
				return;
			}
			INSTALL_PATH = value + "\\"; //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	// Stream de Salida y Error
	private void configureOutput() {
		File errFile = new File(APP_HOME + DIR_ERR);
		File outFile = new File(APP_HOME + DIR_SALIDA);
		PrintStream err = null;
		PrintStream out = null;
		
		try {
			err = new PrintStream(errFile);
			out = new PrintStream(outFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		System.setErr(err);
		System.setOut(out);
		
	}

	// Ventana Principal
	private void initWindow() {
		
		// Inicializar ventana
		frmAplicacion = new JFrame();
		frmAplicacion.setIconImage(Toolkit.getDefaultToolkit().getImage(mainWindow.class.getResource("/img/icono.png"))); //$NON-NLS-1$
		frmAplicacion.setBounds(100, 100, 1200, 600);
		frmAplicacion.setLocationRelativeTo(null);
		frmAplicacion.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAplicacion.getContentPane().setLayout(new BorderLayout(0, 0));
		frmAplicacion.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);

		// Setear fondo de los tabbed panes del color comun del jframe. Asi el Generando queda lindo.
		UIManager.put("TabbedPane.contentAreaColor", frmAplicacion.getBackground());  //$NON-NLS-1$
		
		// Panel Principal	
		tabsArchivos = new JTabbedPane(JTabbedPane.TOP);
		tabsArchivos.setForeground(Color.BLACK);
		tabsArchivos.setBackground(Color.WHITE);
		frmAplicacion.getContentPane().add(tabsArchivos, BorderLayout.CENTER);
		
	}
	
	// Menu Bar
	private void initMenu() {
		// MENU BAR
		menuBar = new JMenuBar();
		menuBar.setFont(new Font("Tahoma", Font.PLAIN, 12)); //$NON-NLS-1$
		frmAplicacion.setJMenuBar(menuBar);
		
		// ARCHIVO
		mnArchivo = new JMenu();
		mnArchivo.setMnemonic('a');
		menuBar.add(mnArchivo);
		
		// Nueva Estructura
		mntmNuevaEstructura = new JMenuItem();
		mntmNuevaEstructura.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mntmNuevaEstructura.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nuevaEstructura();
			}
		});
		mnArchivo.add(mntmNuevaEstructura);
		
		// Abrir
		mntmAbrir = new JMenuItem();
		mntmAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mntmAbrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abrirEstructura();
			}
		});
		mntmAbrir.setMnemonic('a');
		mnArchivo.add(mntmAbrir);
		
		// Guardar
		mntmGuardar = new JMenuItem();
		mntmGuardar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mntmGuardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guardarEstructura();
			}
		});
		mntmGuardar.setMnemonic('g');
		mnArchivo.add(mntmGuardar);
		
		// Guardar Como
		mntmGuardarComo = new JMenuItem();
		mntmGuardarComo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
		mntmGuardarComo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guardarComoEstructura();
			}
		});
		mnArchivo.add(mntmGuardarComo);

		// Separador
		mnArchivo.add(new JSeparator());
		
		// Cerrar Estructura
		mntmCerrarEstructura = new JMenuItem();
		mntmCerrarEstructura.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cerrarEstructura();
			}
		});
		mnArchivo.add(mntmCerrarEstructura);
		
		// Separador
		mnArchivo.add(new JSeparator());
		
		// Salir
		mntmSalir = new JMenuItem();
		mntmSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mntmSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Agregar ventana modal para preguntar si desea guardar.
				//Si esta todo guardado ( incluir booleano ) entonces la aplicacion se tendria que cerrar sin
				//mostrar la ventana modal.
				System.exit(0);
			}
		});	
		mnArchivo.add(mntmSalir);
		
		// BUSCAR
		mnBuscar = new JMenu();
		menuBar.add(mnBuscar);
		
		// Realizar Busqueda
		mntmRealizarBusqueda = new JMenuItem();
		mntmRealizarBusqueda.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
		mntmRealizarBusqueda.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abrirBusqueda();
			}
		});
		mnBuscar.add(mntmRealizarBusqueda);
		
		// Cargar Acciones Desde Archivo
		mntmCargarAcciones = new JMenuItem();
		mntmCargarAcciones.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		mntmCargarAcciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				leerAccionesArchivo();
			}
		});
		mnBuscar.add(mntmCargarAcciones);
		
		// Generar elementos
		mntmGenerarElementos = new JMenuItem();
		mntmGenerarElementos.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		mntmGenerarElementos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abrirGeneracion();
			}
		});
		mnBuscar.add(mntmGenerarElementos);
		
		// IDIOMA
		mnIdioma = new JMenu();
		menuBar.add(mnIdioma);
		
		// Grupo Idiomas
		groupIdioma = new ButtonGroup();
		
		// Idioma es_AR
		rdbtnmntmEspanolargentina = new JRadioButtonMenuItem();
		rdbtnmntmEspanolargentina.setSelected(true);
		rdbtnmntmEspanolargentina.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Messages.setLanguage("es_AR"); //$NON-NLS-1$
				generateMessages();
			}
		});
		mnIdioma.add(rdbtnmntmEspanolargentina);
		rdbtnmntmEspanolargentina.setSelected(true);
		groupIdioma.add(rdbtnmntmEspanolargentina);
		
		// Idioma en_US
//		rdbtnmntmInglesUs = new JRadioButtonMenuItem();
//		rdbtnmntmInglesUs.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				Messages.setLanguage("en_US"); //$NON-NLS-1$
//				generateMessages();
//			}
//		});
//		mnIdioma.add(rdbtnmntmInglesUs);
//		groupIdioma.add(rdbtnmntmInglesUs);
		
		// AYUDA
		mnAyuda = new JMenu();
		menuBar.add(mnAyuda);
		
		// Manual
		mntmAyuda = new JMenuItem();
		mntmAyuda.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		mntmAyuda.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					File file = new File(INSTALL_PATH + DIR_MANUAL);
					Desktop.getDesktop().open(file);
				} catch (Exception e) {}
			}
		});
		mnAyuda.add(mntmAyuda);
		
		// Acerca De
		mntmAcercaDe = new JMenuItem();
		mntmAcercaDe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abrirAyuda();
			}
		});
		mnAyuda.add(mntmAcercaDe);
		
	}

	// Panel Derecho
	private void initPanelDerecho() {
		
		// Inicializar panel
		panelDerecho = new JPanel();
		frmAplicacion.getContentPane().add(panelDerecho, BorderLayout.EAST);
		panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.X_AXIS));
		
		// Crear Box
		verticalBox = Box.createVerticalBox();
		verticalBox.setBorder(new LineBorder(Color.GRAY));
		verticalBox.setPreferredSize(new Dimension(340,0));
		panelDerecho.add(verticalBox);
		
		// Informacion
		initInformacion();
		
		// Separador
		verticalBox.add(new JSeparator());
		
		// Acciones
		initAcciones();
		
		// Separador
		verticalBox.add(new JSeparator());
		
		// Capturas
		initBotonesCaptura();
		
	}

	// Informacion
	private void initInformacion() {
		
		// Titulo
		lblInformacion = new JLabel();
		lblInformacion.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblInformacion.setHorizontalAlignment(SwingConstants.LEFT);
		lblInformacion.setFont(new Font("Verdana", Font.BOLD, 16)); //$NON-NLS-1$
		verticalBox.add(lblInformacion);
		
		// Separador
		verticalBox.add(new JSeparator());
		
		txtInformacion = new JTextPane();
		txtInformacion.setFont(new Font("Tahoma", Font.BOLD, 12)); //$NON-NLS-1$
		txtInformacion.setEditable(false);
		txtInformacion.setPreferredSize(new Dimension(0, 400));
		txtInformacion.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$		
		verticalBox.add(txtInformacion);
		
	}

	// Insertar/Eliminar
	private void initAcciones() {
		// Titulo
		lblAcciones = new JLabel();
		lblAcciones.setFont(new Font("Verdana", Font.BOLD, 16)); //$NON-NLS-1$
		lblAcciones.setHorizontalAlignment(SwingConstants.CENTER);
		lblAcciones.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox.add(lblAcciones);
		
		// Campo de texto
		txtElementos = new JTextField();
		txtElementos.setHorizontalAlignment(JTextField.CENTER); // Centrar texto
		txtElementos.setFont(new Font("Verdana", Font.BOLD, 14)); //$NON-NLS-1$
		txtElementos.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		txtElementos.setColumns(10);
		txtElementos.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e)
			{
				char caracter = e.getKeyChar();

				// Verificar si la tecla pulsada no es un digito
				if (((caracter < '0') || (caracter > '9')) && (caracter != '\b' /*corresponde a BACK_SPACE*/) && (caracter != ',' ) )
				{
					e.consume();  // ignorar el evento de teclado
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (archivo != null && txtElementos.getText().length() > 0){
					btnInsertar.setEnabled(true);
					btnEliminar.setEnabled(true);
				}else{
					btnInsertar.setEnabled(false);
					btnEliminar.setEnabled(false);
				}
			}
		});
		verticalBox.add(txtElementos);
		
		// Layout para ordenar los botones de Insertar, Eliminar y Buscar.
		boxAcciones = Box.createHorizontalBox();
		boxAcciones.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		verticalBox.add(boxAcciones);
		
		// Insertar
		btnInsertar = new JButton();
		btnInsertar.setEnabled(false);
		btnInsertar.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		btnInsertar.setHorizontalAlignment(SwingConstants.LEADING);
		btnInsertar.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnInsertar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Vector<Elemento> elementos = Elemento.parseToElements(txtElementos.getText());
				insertarDatos(elementos);
				txtElementos.setText(""); //$NON-NLS-1$
			}
		});
		boxAcciones.add(btnInsertar);
		
		// Eliminar
		btnEliminar = new JButton();
		btnEliminar.setEnabled(false);
		btnEliminar.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		btnEliminar.setHorizontalAlignment(SwingConstants.TRAILING);
		btnEliminar.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEliminar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<Elemento> elementos = Elemento.parseToElements(txtElementos.getText());
				eliminarDatos(elementos);
				txtElementos.setText(""); //$NON-NLS-1$
			}
		});		
		boxAcciones.add(btnEliminar);
		
	}

	// Captura
	private void initBotonesCaptura() {

		// Titulo
		lblCapturas = new JLabel(Messages.getString("SWING_MAIN_CAPTURAS")); //$NON-NLS-1$
		lblCapturas.setFont(new Font("Verdana", Font.BOLD, 16)); //$NON-NLS-1$
		lblCapturas.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox.add(lblCapturas);
		
		// Layout para ordenar los botones
		boxCapturas = Box.createHorizontalBox();
		boxCapturas.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		verticalBox.add(boxCapturas);		
		
		// Primero
		btnPrimero = new JButton("<<"); //$NON-NLS-1$
		btnPrimero.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		btnPrimero.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnPrimero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (archivo != null){
					archivo.primeraCaptura();
				}
				actualizarImagen();
			}
		});		
		boxCapturas.add(btnPrimero);
		
		// Anterior
		btnAnterior = new JButton("<"); //$NON-NLS-1$
		btnAnterior.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		btnAnterior.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAnterior.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (archivo != null){
					archivo.anteriorCaptura();
				}
				actualizarImagen();
			}
		});
		boxCapturas.add(btnAnterior);

		// Siguiente
		btnSiguiente = new JButton(">"); //$NON-NLS-1$
		btnSiguiente.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		btnSiguiente.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSiguiente.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (archivo != null){
					archivo.siguienteCaptura();
				}
				actualizarImagen();
			}
		});
		boxCapturas.add(btnSiguiente);
		
		// Ultimo
		btnUltimo = new JButton(">>"); //$NON-NLS-1$
		btnUltimo.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		btnUltimo.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnUltimo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (archivo != null){
					archivo.ultimaCaptura();
				}
				actualizarImagen();
			}
		});
		boxCapturas.add(btnUltimo);	
		
	}
	
	// Consola
	private void initConsola() {
		// Consola
		txtConsola = new JTextPane();
		txtConsola.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		txtConsola.setEditable(false);
		txtConsola.setPreferredSize(new Dimension(0, 175));
		txtConsola.setDoubleBuffered(true);
		txtConsola.setFont(new Font("Verdana", Font.BOLD, 12)); //$NON-NLS-1$
		frmAplicacion.getContentPane().add(txtConsola, BorderLayout.SOUTH);
		
		// Scroll de la Consola
	    scrollConsola = new JScrollPane(txtConsola);
		scrollConsola.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollConsola.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollConsola.setPreferredSize(new Dimension(0,175));
		frmAplicacion.getContentPane().add(scrollConsola, BorderLayout.SOUTH);
		
		scrollConsola.setColumnHeaderView(panelConsola);
		GridBagLayout gbl_PanelConsola = new GridBagLayout();
		gbl_PanelConsola.columnWidths = new int[]{94, 19, 19, 19, 0};
		gbl_PanelConsola.rowHeights = new int[]{21, 0};
		gbl_PanelConsola.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_PanelConsola.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelConsola.setLayout(gbl_PanelConsola);
		
		// Titulo
		lblConsola = new JLabel();
		lblConsola.setHorizontalAlignment(SwingConstants.LEFT);
		lblConsola.setAlignmentX(1.0f);
		lblConsola.setFont(new Font("Verdana", Font.BOLD, 16)); //$NON-NLS-1$
		
		// Constraint Titulo
		GridBagConstraints gbc_Cons = new GridBagConstraints();
		gbc_Cons.anchor = GridBagConstraints.EAST;
		gbc_Cons.insets = new Insets(0, 0, 0, 5);
		gbc_Cons.gridx = 0;
		gbc_Cons.gridy = 0;
		panelConsola.add(lblConsola, gbc_Cons);	
		
		// Boton de Borrar consola
		btnBorrar.setBorder(null);
		// Insets !! Importante para que el boton sea chiquito.
		btnBorrar.setMargin(new Insets(2, 2, 2, 2));
		btnBorrar.setSelectedIcon(new ImageIcon(mainWindow.class.getResource("/img/borrar_consola.png"))); //$NON-NLS-1$
		btnBorrar.setIcon(new ImageIcon(mainWindow.class.getResource("/img/borrar_consola.png"))); //$NON-NLS-1$
		btnBorrar.setPreferredSize(new Dimension(19, 18));
		btnBorrar.setMargin(new Insets(0, 0, 0, 0));
		btnBorrar.setAlignmentX(Component.RIGHT_ALIGNMENT);
		btnBorrar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				txtConsola.setText(""); //$NON-NLS-1$
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
				btnBorrar.setBorder(new LineBorder(Color.GRAY, 1));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnBorrar.setBorder(null);
			}
		});
		
		// Contraint Boton Borrar
		GridBagConstraints gbc_Borrar = new GridBagConstraints();
		gbc_Borrar.anchor = GridBagConstraints.EAST;
		gbc_Borrar.insets = new Insets(0, 0, 0, 5);
		gbc_Borrar.gridx = 1;
		gbc_Borrar.gridy = 0;
		panelConsola.add(btnBorrar, gbc_Borrar);
		
		// Boton de Minimizar Consola
		btnMin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if ( btnMin.isEnabled() )
				btnMin.setBorder(new LineBorder(Color.GRAY, 1));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnMin.setBorder(null);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				//Achicar la consola aca.
				txtConsola.setPreferredSize(new Dimension( java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,0));
				scrollConsola.setPreferredSize(new Dimension( java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,24));
				frmAplicacion.pack();
				frmAplicacion.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
				btnMax.setEnabled(true);
				btnMin.setEnabled(false);
			}
		});
		btnMin.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnMin.setBorder(null);
		//Insets !! Importante para que el boton sea chiquito.
		btnMin.setMargin(new java.awt.Insets(2, 2, 2, 2));
		btnMin.setPreferredSize(new Dimension(19, 18));
		btnMin.setMargin(new Insets(0, 0, 0, 0));
		btnMin.setIcon(new ImageIcon(mainWindow.class.getResource("/img/minimizar.png"))); //$NON-NLS-1$

		// Constraint Boton Minimizar
		GridBagConstraints gbc_Min = new GridBagConstraints();
		gbc_Min.insets = new Insets(0, 0, 0, 5);
		gbc_Min.gridx = 2;
		gbc_Min.gridy = 0;
		panelConsola.add(btnMin, gbc_Min);
		
		// Boton de maximizar consola
		btnMax.setEnabled(false);
		btnMax.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnMax.setBorder(null);
		//Insets !! Importante para que el boton sea chiquito.
		btnMax.setMargin(new java.awt.Insets(2, 2, 2, 2));
		btnMax.setPreferredSize(new Dimension(19, 18));
		btnMax.setMargin(new Insets(0, 0, 0, 0));
		btnMax.setIcon(new ImageIcon(mainWindow.class.getResource("/img/maximizar.png"))); //$NON-NLS-1$
		btnMax.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtConsola.setPreferredSize(new Dimension( java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,175));
				scrollConsola.setPreferredSize(new Dimension( java.awt.Toolkit.getDefaultToolkit().getScreenSize().width,175));
				frmAplicacion.pack();
				frmAplicacion.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
				btnMax.setEnabled(false);
				btnMin.setEnabled(true);
			}
		});
		btnMax.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				if ( btnMax.isEnabled() )
				btnMax.setBorder(new LineBorder(Color.GRAY, 1));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				btnMax.setBorder(null);
			}
		});
		
		// Constraint Boton Maximizar
		GridBagConstraints gbc_Max = new GridBagConstraints();
		gbc_Max.gridx = 3;
		gbc_Max.gridy = 0;
		panelConsola.add(btnMax, gbc_Max);
		
	}
	
	private void generateMessages() {
		frmAplicacion.setTitle(Messages.getString("SWING_MAIN_TITULO")); //$NON-NLS-1$
		
		lblInformacion.setText(Messages.getString("SWING_MAIN_INFORMACION")); //$NON-NLS-1$
		
		// Panel Derecho
		
		// Acciones
		lblAcciones.setText(Messages.getString("SWING_MAIN_INSERTAR_ELIMINAR")); //$NON-NLS-1$
		btnInsertar.setText(Messages.getString("SWING_MAIN_INSERTAR")); //$NON-NLS-1$
		btnEliminar.setText(Messages.getString("SWING_MAIN_ELIMINAR")); //$NON-NLS-1$
		
		// Capturas
		btnPrimero.setToolTipText(Messages.getString("SWING_MAIN_PRIMERA_CAPTURA")); //$NON-NLS-1$
		btnAnterior.setToolTipText(Messages.getString("SWING_MAIN_CAPTURA_ANTERIOR")); //$NON-NLS-1$
		btnSiguiente.setToolTipText(Messages.getString("SWING_MAIN_CAPTURA_SIGUIENTE")); //$NON-NLS-1$
		btnUltimo.setToolTipText(Messages.getString("SWING_MAIN_ULTIMA_CAPTURA")); //$NON-NLS-1$
		
		txtConsola.setToolTipText(Messages.getString("SWING_MAIN_CONSOLA")); //$NON-NLS-1$
		txtConsola.setText(Messages.getString("SWING_MAIN_BIENVENIDO")); //$NON-NLS-1$
		btnBorrar.setToolTipText(Messages.getString("SWING_MAIN_BORRAR_CONSOLA")); //$NON-NLS-1$
		lblConsola.setText(Messages.getString("SWING_MAIN_CONSOLA")); //$NON-NLS-1$
		btnMin.setToolTipText(Messages.getString("SWING_MAIN_MINIMIZAR_CONSOLA")); //$NON-NLS-1$
		btnMax.setToolTipText(Messages.getString("SWING_MAIN_MAXIMIZAR_CONSOLA")); //$NON-NLS-1$
		
		// Menu Bar
		mnArchivo.setText(Messages.getString("SWING_MAIN_ARCHIVO")); //$NON-NLS-1$
		mntmNuevaEstructura.setText(Messages.getString("SWING_MAIN_NUEVA_ESTRUCTURA")); //$NON-NLS-1$
		mntmAbrir.setText(Messages.getString("SWING_MAIN_ABRIR")); //$NON-NLS-1$
		mntmGuardar.setText(Messages.getString("SWING_MAIN_GUARDAR")); //$NON-NLS-1$
		mntmGuardarComo.setText(Messages.getString("SWING_MAIN_GUARDAR_COMO")); //$NON-NLS-1$
		mntmCerrarEstructura.setText(Messages.getString("SWING_MAIN_CERRAR")); //$NON-NLS-1$
		mntmSalir.setText(Messages.getString("SWING_MAIN_SALIR")); //$NON-NLS-1$
		mnBuscar.setText(Messages.getString("SWING_MAIN_BUSCAR")); //$NON-NLS-1$
		mntmRealizarBusqueda.setText(Messages.getString("SWING_MAIN_REALIZAR_BUSQUEDA")); //$NON-NLS-1$
		mntmCargarAcciones.setText(Messages.getString("SWING_MAIN_CARGAR_DESDE_ARCHIVO")); //$NON-NLS-1$
		mntmGenerarElementos.setText(Messages.getString("SWING_MAIN_GENERAR")); //$NON-NLS-1$
		mnIdioma.setText(Messages.getString("SWING_MAIN_IDIOMA")); //$NON-NLS-1$
		rdbtnmntmEspanolargentina.setText(Messages.getString("SWING_MAIN_ES_AR")); //$NON-NLS-1$
//		rdbtnmntmInglesUs.setText(Messages.getString("SWING_MAIN_EN_US")); //$NON-NLS-1$
		mnAyuda.setText(Messages.getString("SWING_MAIN_AYUDA")); //$NON-NLS-1$
		mntmAyuda.setText(Messages.getString("SWING_MAIN_AYUDA_MANUAL")); //$NON-NLS-1$
		mntmAcercaDe.setText(Messages.getString("SWING_MAIN_ACERCA_DE")); //$NON-NLS-1$
	}

	public void actualizarImagen() {
		// Bloquear botones
		btnPrimero.setEnabled(false);
		btnAnterior.setEnabled(false);
		btnSiguiente.setEnabled(false);
		btnUltimo.setEnabled(false);
		
		if (archivo != null){
			// Renderizar imagen actual
			archivo.generateGraph();
			archivo.actualizar();
			
			// Actualizar botones de captura
			btnPrimero.setEnabled(archivo.canAnteriorCaptura());
			btnAnterior.setEnabled(archivo.canAnteriorCaptura());
			btnSiguiente.setEnabled(archivo.canSiguienteCaptura());
			btnUltimo.setEnabled(archivo.canSiguienteCaptura());
		}else{
			System.out.println(Messages.getString("SWING_MAIN_NINGUNA_ESTRUCTURA_ACTUALIZAR")); //$NON-NLS-1$
		}
		
	}
	
	/***********************************************************************************
	 *                                                                                 *
	 *                            TODO ACCIONES ESTRUCTURA                             *
	 *                                                                                 *
	 ***********************************************************************************/
	
	protected void leerAccionesArchivo() {
		
		// Crear un objeto FileChooser
        JFileChooser fc = new JFileChooser();
        
        // Mostrar la ventana para abrir archivo y recoger la respuesta
        fc.setFileFilter(new FiltroArchivoTexto());
        int respuesta = fc.showOpenDialog(null);
        
        // Comprobar si se ha pulsado Aceptar
        if (respuesta == JFileChooser.APPROVE_OPTION){
            // Crear un objeto File con el archivo elegido
            File arch = fc.getSelectedFile();
            
            // Mostrar el nombre del archivo en un campo de texto
            System.out.println(arch.getPath());

            // Cargar datos en archivo
            archivo.load(this, arch);
		
        }
        
	}

	protected void nuevaEstructura() {
		
		//Borramos la consola.
		txtConsola.setText(""); //$NON-NLS-1$
		
		if (archivo == null){
			//Logger.getLogger("ARCHIVO").info("Creando nueva estructura");
			
			// Construir archivo nuevo
			archivo = new Archivo(JTabbedPane.BOTTOM, this);
			
			// Agregar pestaña referida al archivo
			archivo.setBackground(Color.WHITE);
			archivo.setForeground(Color.BLACK);
	        
	        // Asistente para configurar archivo		
			DialogNuevaEstructura nuevaEstructura = new DialogNuevaEstructura(this, archivo);
			nuevaEstructura.setVisible(true);
			nuevaEstructura.setAlwaysOnTop(true);
			nuevaEstructura.setModal(true);
			nuevaEstructura.setModalityType(ModalityType.APPLICATION_MODAL);			
		}else{
			cerrarEstructura();
			nuevaEstructura();
		}		
	}
	
	protected void abrirEstructura() {

		//Borramos la consola.
		txtConsola.setText(""); //$NON-NLS-1$
		
		if (archivo == null){

			//Logger.getLogger("ARCHIVO").info("Abrir archivo");
			
			//Crear un objeto FileChooser
	        JFileChooser fc = new JFileChooser();
	        
	        //Mostrar la ventana para abrir archivo y recoger la respuesta
	        fc.setFileFilter(new FiltroArchivoEstructura());
	        int respuesta = fc.showOpenDialog(null);
	        
	        //Comprobar si se ha pulsado Aceptar
	        if (respuesta == JFileChooser.APPROVE_OPTION){
	        	abrirEstructura(fc.getSelectedFile());
	        	
	            /*//Crear un objeto File con el archivo elegido
	            File archivoElegido = fc.getSelectedFile();
	            
	            //Mostrar el nombre del archivo en un campo de texto
	            System.out.println(archivoElegido.getPath());
	            	            	            
	            // Intenta cargar archivo y mostrarlo en pantalla
	    		archivo = new Archivo(JTabbedPane.BOTTOM, this);
	    		
	    		// Cargar
	    		archivo.load(this, archivoElegido);
	    		
//	    		if(archivo.cargar(archivoElegido.getPath())){
//
		            // Agregar pesta�a referida al archivo
//		    		archivo.setBackground(Color.WHITE);
		    		tabsArchivos.addTab(archivoElegido.getName(), archivo);
		    		
		    		// Agrega vistas al archivo
		    		archivo.ultimaCaptura();
					actualizarImagen();	
		    		archivo.agregarTab();
//	    		}else{
//	    			archivo = null;
//	    		}*/
	        }
		}else{
			cerrarEstructura();
			abrirEstructura();
		}		
	}
	
	protected void abrirEstructura(File archivoElegido){
		
		// Intenta cargar archivo y mostrarlo en pantalla
		archivo = new Archivo(JTabbedPane.BOTTOM, this);
		
		// Cargar
		archivo.load(this, archivoElegido);
		
//		if(archivo.cargar(archivoElegido.getPath())){
//
            // Agregar pestaña referida al archivo
    		tabsArchivos.addTab(archivoElegido.getName(), archivo);
    		
    		// Agrega vistas al archivo
    		archivo.ultimaCaptura();
			actualizarImagen();
    		archivo.agregarTab();
//		}else{
//			archivo = null;
//		}
	}
	
	protected void guardarEstructura(){
		if (archivo != null){
			//Logger.getLogger("ARCHIVO").info("Guardar archivo");
			
			// Verificar si tiene referencia a una direccion
			if (archivo.getPath() != null){
				archivo.guardar();
			}else{
				guardarComoEstructura();
			}
		}else{
			//Logger.getLogger("ARCHIVO").info("Ningun archivo a guardar");
		}
	}

	protected void guardarComoEstructura() {
		if (archivo != null){		
			//Logger.getLogger("ARCHIVO").info("Guardando archivo como");

			// Crear un objeto FileChooser
			JFileChooser fc = new JFileChooser();

			// Mostrar la ventana para abrir archivo y recoger la respuesta
			fc.setFileFilter(new FiltroArchivoEstructura());
			int respuesta = fc.showSaveDialog(null);

			// Comprobar si se ha pulsado Aceptar
			if (respuesta == JFileChooser.APPROVE_OPTION){
				// Crear un objeto File con el archivo elegido
				File archivoElegido = fc.getSelectedFile();
				// Mostrar el nombre del archvivo en un campo de texto
				System.out.println(archivoElegido.getPath());

				archivo.guardar(archivoElegido.getPath(), archivoElegido.getName());
				tabsArchivos.setTitleAt(tabsArchivos.getSelectedIndex(), archivoElegido.getName());
			}	
		}	
	}
	
	protected void cerrarEstructura() {
		if ( (archivo != null) && (archivo.isChanged()) ){
			// Preguntar si desea guardar los cambios
			//Logger.getLogger("ARCHIVO").warn("¿Desea guardar los cambios?");
			//esto desps sacarlo que esta mal
			archivo = null;
		}else{
			//Logger.getLogger("ARCHIVO").info("Archivo cerrado sin cambios");
			archivo = null;
		}
		
		//Aca tenemos que sacar los tabs.
		if ( tabsArchivos.getSelectedIndex() >= 0)
		tabsArchivos.remove(tabsArchivos.getSelectedIndex());
		//Borramos el panel de informacion.
		txtInformacion.setText(""); //$NON-NLS-1$		

		// Bloquear botones
		btnPrimero.setEnabled(false);
		btnAnterior.setEnabled(false);
		btnSiguiente.setEnabled(false);
		btnUltimo.setEnabled(false);
	}
	
	/***********************************************************************************
	 *                                                                                 *
	 *                          TODO INSERCION / ELIMINACION                           *
	 *                                                                                 *
	 ***********************************************************************************/
	
	protected void insertarDatos(Vector<Elemento> elementos) {
		if ( (archivo != null) && (elementos != null) && (elementos.size() > 0) ){

			long tiempoInicial = System.currentTimeMillis();

			// Insertar elementos en el archivo
			String info = "Insertar";
			archivo.insertar(elementos.get(0));
			info += " " + elementos.get(0);
			for (int i = 1; i < elementos.size(); i++){
				archivo.insertar(elementos.get(i));
				info += "," + elementos.get(i);
			}
			archivo.addInfo(info);

			long tiempoMedio = System.currentTimeMillis();

			// Actualizar pantalla
			archivo.ultimaCaptura();
			actualizarImagen();

			long tiempoFinal = System.currentTimeMillis();
			
			System.out.println("Insertar datos: " + (tiempoMedio - tiempoInicial)); //$NON-NLS-1$
			System.out.println("Mostrar en pantalla: " + (tiempoFinal - tiempoMedio)); //$NON-NLS-1$
		}else{
			System.out.println(Messages.getString("SWING_MAIN_NINGUN_ARCHIVO_CARGADO_INSERTAR")); //$NON-NLS-1$
		}
	}
	
	protected void eliminarDatos(Vector<Elemento> elementos) {
		if ( (archivo != null) && (elementos != null) && (elementos.size() > 0) ){

			long tiempoInicial = System.currentTimeMillis();

			// Insertar elementos en el archivo
			String info = "Eliminar";
			archivo.eliminar(elementos.get(0));
			info += " " + elementos.get(0);
			for (int i = 1; i < elementos.size(); i++){
				archivo.eliminar(elementos.get(i));
				info += "," + elementos.get(i);
			}
			archivo.addInfo(info);

			long tiempoMedio = System.currentTimeMillis();

			// Actualizar pantalla
			archivo.ultimaCaptura();
			actualizarImagen();

			long tiempoFinal = System.currentTimeMillis();
			
			System.out.println("Eliminar datos: " + (tiempoMedio - tiempoInicial)); //$NON-NLS-1$
			System.out.println("Mostrar en pantalla: " + (tiempoFinal - tiempoMedio)); //$NON-NLS-1$
		}else{
			System.out.println(Messages.getString("SWING_MAIN_NINGUN_ARCHIVO_CARGADO_ELIMINAR")); //$NON-NLS-1$
		}
	}

	/***********************************************************************************
	 *                                                                                 *
	 *                                TODO HERRAMIENTAS                                *
	 *                                                                                 *
	 ***********************************************************************************/
	
	private void abrirAyuda(){
		// Asistente para configurar archivo
		DialogAcercaDe about = new DialogAcercaDe(this);
		about.setVisible(true);
		about.setAlwaysOnTop(true);
		about.setModal(true);
		about.setModalityType(ModalityType.APPLICATION_MODAL);	
		
	}
	
	private void abrirBusqueda() {	
		if (archivo != null){
			Vector<Almacenamiento> almac = archivo.getAlmac();
			if ( almac != null && almac.size() > 0 ){
				DialogBuscar b = new DialogBuscar(this,almac);
				b.setVisible(true);
				b.setAlwaysOnTop(true);
				b.setModal(true);
				b.setModalityType(ModalityType.APPLICATION_MODAL);	
			}
		}
	}
	

	private void abrirGeneracion() {
		if (archivo != null){
			//DialogBuscar b = new DialogBuscar(this,almac);
			DialogGenerarElementos b = new DialogGenerarElementos(this, archivo);
			b.setVisible(true);
			b.setAlwaysOnTop(true);
			b.setModal(true);
			b.setModalityType(ModalityType.APPLICATION_MODAL);
		}	
	}

}
