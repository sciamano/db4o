/* Copyright (C) 2004 - 2009  Versant Inc.  http://www.db4o.com */
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using EnvDTE;
using System.Reflection;
using EnvDTE80;
using OManager.BusinessLayer.UIHelper;
using OMControlLibrary.Common;
using OManager.BusinessLayer.Login;
using Microsoft.VisualStudio.CommandBars;
using OME.Logging.Common;
using stdole;
using Constants = OMControlLibrary.Common.Constants;


namespace OMControlLibrary
{
	/// <summary>
	/// Using this user control, user can login to ObjectManager Enterprise.
	/// </summary>

	[ComVisible(true)]
	public partial class Login: ILoadData
	{
		#region Member Variables

		//Private static variables
	    private static Window loginToolWindow;
		private static CommandBarControl m_cmdBarCtrlCreateDemoDb;
		internal static CommandBarControl m_cmdBarCtrlConnect;
		internal static CommandBarControl m_cmdBarCtrlBackup;
		internal static CommandBarButton m_cmdBarBtnConnect;
		private static Assembly m_AddIn_Assembly;
		//Private variables
		private IList<RecentQueries> m_recentConnections;

		//Constants

        private const string IMAGE_DISCONNECT = "OMAddin.Images.DB_DISCONNECT2_a.GIF";
        private const string IMAGE_DISCONNECT_MASKED = "OMAddin.Images.DB_DISCONNECT2_b.BMP";

		private const string OPEN_FILE_DIALOG_FILTER = "db4o Database Files(*.yap, *.db4o)|*.yap;*.db4o|All Files(*.*)|*.*";
		private const string STRING_SERVER = "server:";
		private const string STRING_COLON = ":";
		private const char CHAR_COLON = ':';

		static Window queryBuilderToolWindow;
	

		#endregion

		#region Constructor

		/// <summary>
		/// Constructor for Login
		/// </summary>
		public Login()
		{
			InitializeComponent();
		
		}

		

		#endregion
		#region Properties
		

		#endregion
		#region Methods

		#region Public

		#region SetLiterals()
		/// <summary>
		/// A method from ViewBase class overriden for setting the text to all the labels.
		/// </summary>
		public override void SetLiterals()
		{
			try
			{
				labelFile.Text = Helper.GetResourceString(Common.Constants.LOGIN_RECENTCONNECTION_TEXT);
				labelHost.Text = Helper.GetResourceString(Common.Constants.LOGIN_HOST_TEXT);
				labelPort.Text = Helper.GetResourceString(Common.Constants.LOGIN_PORT_TEXT);
				labelUserName.Text = Helper.GetResourceString(Common.Constants.LOGIN_USERNAME_TEXT);
				labelPassword.Text = Helper.GetResourceString(Common.Constants.LOGIN_PASSWORD_TEXT);
				labelType.Text = Helper.GetResourceString(Common.Constants.LOGIN_TYPE_TEXT);
				labelNewConnection.Text = Helper.GetResourceString(Common.Constants.LOGIN_NEWCONNECTION_TEXT);
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region CreateLoginToolWindow()
		/// <summary>
		/// Creates the login tool window.
		/// </summary>
		public static void CreateLoginToolWindow(CommandBarControl cmdBarCtrl,
			CommandBarButton cmdBarBtn, Assembly addIn_Assembly,
			CommandBarControl cmdBarCtrlBackup, CommandBarControl dbCreateDemoDbControl)
		{
			try
			{
				m_AddIn_Assembly = addIn_Assembly;
				m_cmdBarCtrlConnect = cmdBarCtrl;
				m_cmdBarBtnConnect = cmdBarBtn;
				m_cmdBarCtrlBackup = cmdBarCtrlBackup;
				m_cmdBarCtrlCreateDemoDb = dbCreateDemoDbControl;

                loginToolWindow = CreateToolWindow(Common.Constants.CLASS_NAME_LOGIN, Common.Constants.LOGIN, NewFormattedGuid());

                if (loginToolWindow.AutoHides)
				{
                    loginToolWindow.AutoHides = false;
				}
                loginToolWindow.Visible = true;
                loginToolWindow.Width = 425;
                loginToolWindow.Height = 170;
				Helper.CheckIfLoginWindowIsVisible = true;
				
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}

		private static string NewFormattedGuid()
		{
			return Guid.NewGuid().ToString(Helper.GetResourceString(Common.Constants.GUID_FORMATTER_STRING));
		}

		#endregion



		public static void CreateQueryBuilderToolWindow()
		{
			try
			{
				string caption = Helper.GetResourceString(Common.Constants.QUERY_BUILDER_CAPTION);

				queryBuilderToolWindow = CreateToolWindow(Common.Constants.CLASS_NAME_QUERYBUILDER, caption, Common.Constants.GUID_QUERYBUILDER );
				
				if (queryBuilderToolWindow.AutoHides)
				{
					queryBuilderToolWindow.AutoHides = false;
				}
				queryBuilderToolWindow.IsFloating = false;
				queryBuilderToolWindow.Linkable = false;
				queryBuilderToolWindow.Visible = true;
				
			}
			catch (Exception e)
			{
				LoggingHelper.HandleException(e); 
			}
		}
		


		#endregion



		#region Private

		 public void LoadAppropriatedata()
		{



			//Events2 eventsSource = (Events2)ApplicationObject.Events;
			//_events = eventsSource.get_WindowVisibilityEvents(null);
			//_events.WindowHiding += OnWindowHidding;
			//_events.WindowShowing += OnWindowShowing;

			toolTipForTextBox.RemoveAll();
			comboBoxFilePath.Items.Clear();
			textBoxConnection.Text = "";
			m_recentConnections = GetAllRecentConnections();
			if (m_recentConnections != null)
			{
				foreach (RecentQueries recentQuery in m_recentConnections)
				{
					if (recentQuery.ConnParam.Host != null)
					{
						ShowAppropriatePanel(false);
						PopulateRemoteRecentConnections();

					}
					else
					{
						m_cmdBarCtrlBackup.Enabled = false;
					    m_cmdBarCtrlCreateDemoDb.Enabled = true;
						ShowAppropriatePanel(true);
						PopulateLocalRecentConnections();
					}
					break;
				}
				if (comboBoxFilePath.Items.Count > 1)
				{
					comboBoxFilePath.SelectedIndex = 1;
				}
			}
		}

		

		private void ShowAppropriatePanel(bool param)
		{
			panelLocal.Visible = param;
			panelRemote.Visible = !param;
			radioButtonLocal.Checked = param;
			radioButtonRemote.Checked = !param;
		}

		#region AfterSuccessfullyConnected()
		private void AfterSuccessfullyConnected()
		{
			try
			{
				m_cmdBarCtrlConnect.Caption = Common.Constants.TOOLBAR_DISCONNECT;		

				m_cmdBarBtnConnect.Caption = Common.Constants.TOOLBAR_DISCONNECT;
				m_cmdBarBtnConnect.TooltipText = Common.Constants.TOOLBAR_DISCONNECT;
				

				if (radioButtonLocal.Checked)
				{
					m_cmdBarCtrlBackup.Enabled = true;
					m_cmdBarCtrlCreateDemoDb.Enabled = false;
				}
				Helper.SetPicture(m_AddIn_Assembly, (CommandBarButton)m_cmdBarCtrlConnect.Control, IMAGE_DISCONNECT, IMAGE_DISCONNECT_MASKED);
				Helper.SetPicture(m_AddIn_Assembly, (CommandBarButton)m_cmdBarBtnConnect.Control, IMAGE_DISCONNECT, IMAGE_DISCONNECT_MASKED);
				
#if !NET_4_0
                ((CommandBarButton)m_cmdBarCtrlConnect).State = MsoButtonState.msoButtonDown;
				m_cmdBarBtnConnect.State = MsoButtonState.msoButtonDown;
#endif

			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region PopulateLocalRecentConnections()
		private void PopulateLocalRecentConnections()
		{
			try
			{
				if (m_recentConnections == null)
					m_recentConnections = GetAllRecentConnections();

				if (m_recentConnections.Count > 0)
				{
					comboBoxFilePath.Items.Clear();
					comboBoxFilePath.Items.Add(Helper.GetResourceString(Common.Constants.COMBOBOX_DEFAULT_TEXT));
					foreach (RecentQueries recentQuery in m_recentConnections)
					{
						if (recentQuery.ConnParam.Host == null)
							comboBoxFilePath.Items.Add(recentQuery.ConnParam.Connection);
					}
					comboBoxFilePath.SelectedIndex = 0;
				}
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region PopulateRemoteRecentConnections()
		private void PopulateRemoteRecentConnections()
		{
			try
			{
				if (m_recentConnections == null)
					m_recentConnections = GetAllRecentConnections();
                if (m_recentConnections != null)
                {
                    if (m_recentConnections.Count > 0)
                    {
                        comboBoxFilePath.Items.Clear();
                        comboBoxFilePath.Items.Add(Helper.GetResourceString(Common.Constants.COMBOBOX_DEFAULT_TEXT));
                        foreach (RecentQueries recentQuery in m_recentConnections)
                        {
                            if (recentQuery.ConnParam.Host != null)
                                comboBoxFilePath.Items.Add(recentQuery.ConnParam.Connection);
                        }
                        comboBoxFilePath.SelectedIndex = 0;
                    }
                }
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region GetAllRecentConnections()
		private static List<RecentQueries> GetAllRecentConnections()
		{
			List<RecentQueries> recentConnections = new List<RecentQueries>();
			try
			{
				recentConnections = dbInteraction.FetchRecentQueries();
				if (recentConnections != null)
				{
					CompareTimestamps comparator = new CompareTimestamps();
					recentConnections.Sort(comparator);
				}
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
			return recentConnections;
		}
		#endregion

		#endregion

		#endregion

		#region Event Handlers

		#region Login_Load
		/// <summary>
		/// Sets the label text.
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		private void Login_Load(object sender, EventArgs e)
		{
			try
			{
			
				LoadAppropriatedata();
				SetLiterals();
				textBoxConnection.Clear();
				textBoxHost.Clear();
				textBoxPassword.Clear();
				textBoxPort.Clear();
				textBoxPassword.Clear();
				
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}

		
		

		

		
		#endregion

		#region radioButton_Click
		/// <summary>
		/// Event handler for toggling between Local Connection & Remote Connection.
		/// </summary>
		/// <param name="sender">The event can be invoked by either Local or remote radio button</param>
		/// <param name="e"></param>
		private void radioButton_Click(object sender, EventArgs e)
		{
			try
			{

				comboBoxFilePath.Items.Clear();
				toolTipForTextBox.RemoveAll();
				if (radioButtonLocal.Checked)
				{
					textBoxConnection.Clear();
					panelLocal.Visible = true;
					panelRemote.Visible = false;
					buttonConnect.Text = Helper.GetResourceString(Common.Constants.LOGIN_CAPTION_OPEN);
					PopulateLocalRecentConnections();
				}
				else
				{
					textBoxHost.Clear();
					textBoxPort.Clear();
					textBoxUserName.Clear();
					textBoxPassword.Clear();
					panelLocal.Visible = false;
					panelRemote.Visible = true;
					buttonConnect.Text = Helper.GetResourceString(Common.Constants.LOGIN_CAPTION_CONNECT);
					PopulateRemoteRecentConnections();
					m_cmdBarCtrlBackup.Enabled = false;
					m_cmdBarCtrlCreateDemoDb.Enabled = true;
				}
				if (comboBoxFilePath.Items.Count > 1)
				{
					comboBoxFilePath.SelectedIndex = 1;
				}
				
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region buttonBrowse_Click
		/// <summary>
		/// Event handler for browsing. This is used for Local Connection to select the database file. 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		private void buttonBrowse_Click(object sender, EventArgs e)
		{
			try
			{
				if (comboBoxFilePath.Items.Count > 0)
				{
					comboBoxFilePath.SelectedIndex = 0;
					textBoxConnection.Clear();
				}
				openFileDialog.Filter = OPEN_FILE_DIALOG_FILTER;
				openFileDialog.Title = Helper.GetResourceString(Common.Constants.LOGIN_OPEN_FILE_DIALOG_CAPTION);  //OPEN_FILE_DIALOG_TITLE;
				if (openFileDialog.ShowDialog() != DialogResult.Cancel)
				{
					textBoxConnection.Text = openFileDialog.FileName;
					toolTipForTextBox.SetToolTip(textBoxConnection, textBoxConnection.Text);
					if (comboBoxFilePath.Items.Contains(textBoxConnection.Text))
						comboBoxFilePath.SelectedItem = textBoxConnection.Text;
					buttonConnect.Focus();
				}
				else
				{
					if (comboBoxFilePath.Items.Count > 0)
					{
						comboBoxFilePath.SelectedText = Helper.GetResourceString(Constants.COMBOBOX_DEFAULT_TEXT);  
						//comboBoxFilePath.SelectedIndex = 1;
					}
				}
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region buttonConnect_Click
		private void buttonConnect_Click(object sender, EventArgs e)
		{
			ConnParams conparam = null;
			RecentQueries currRecentQueries = null;
			string exceptionString = string.Empty;
			try
			{
				
				if(radioButtonLocal.Checked )
				{

					if (!(Validations.ValidateLocalLoginParams(ref comboBoxFilePath, ref textBoxConnection)))
						return;
					try
					{
						conparam = new ConnParams(textBoxConnection.Text.Trim());
					}
					catch (Exception oEx)
					{
						LoggingHelper.ShowMessage(oEx);
					}
				}
				else // Remote Connection
				{
					if (!(Validations.ValidateRemoteLoginParams(ref comboBoxFilePath, ref textBoxHost, ref textBoxPort, ref textBoxUserName, ref textBoxPassword)))
						return;
					try
					{
						string connection = STRING_SERVER + textBoxHost.Text.Trim() + STRING_COLON + textBoxPort.Text.Trim() + STRING_COLON + textBoxUserName.Text.Trim();
						conparam = new ConnParams(connection, textBoxHost.Text.Trim(), textBoxUserName.Text.Trim(), textBoxPassword.Text.Trim(), Convert.ToInt32(textBoxPort.Text.Trim()));
					}
					catch (Exception oEx)
					{
						LoggingHelper.ShowMessage(oEx);
					}
				}
				try
				{
					currRecentQueries = new RecentQueries(conparam);
					RecentQueries tempRecentQueries = currRecentQueries.ChkIfRecentConnIsInDb();
					if (tempRecentQueries != null)
						currRecentQueries = tempRecentQueries;
 
					exceptionString = dbInteraction.ConnectoToDB(currRecentQueries);
				}
				catch (Exception oEx)
				{
					LoggingHelper.ShowMessage(oEx);
				}
				
				if (exceptionString == string.Empty)
				{
					dbInteraction.SetCurrentRecentConnection(currRecentQueries);
					dbInteraction.SaveRecentConnection(currRecentQueries);
					AfterSuccessfullyConnected();

                    loginToolWindow.Close(vsSaveChanges.vsSaveChangesNo);
					Helper.CheckIfLoginWindowIsVisible = false;
					ObjectBrowserToolWin.CreateObjectBrowserToolWindow();
					ObjectBrowserToolWin.ObjBrowserWindow.Visible = true;

					PropertyPaneToolWin.CreatePropertiesPaneToolWindow(true);
					PropertyPaneToolWin.PropWindow.Visible = true;

					CreateQueryBuilderToolWindow();
					
				}
				else
				{
					//buttonConnect.Enabled = true;
					textBoxConnection.Clear();
					MessageBox.Show(exceptionString,
						Helper.GetResourceString(Common.Constants.PRODUCT_CAPTION),
						MessageBoxButtons.OK,
						MessageBoxIcon.Error);
					return;
				}

			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		#region comboBoxFilePath_SelectedIndexChanged
		private void comboBoxFilePath_SelectedIndexChanged(object sender, EventArgs e)
		{
			try
			{
				if (radioButtonRemote.Checked)
				{
					if (!comboBoxFilePath.Text.Equals(Helper.GetResourceString(Common.Constants.COMBOBOX_DEFAULT_TEXT)))
					{
						string[] strRemote = comboBoxFilePath.Text.Split(CHAR_COLON);
						textBoxHost.Text = strRemote[1];
						textBoxPort.Text = strRemote[2];
						textBoxUserName.Text = strRemote[3];
						textBoxPassword.Focus();
						toolTipForTextBox.SetToolTip(comboBoxFilePath, comboBoxFilePath.SelectedItem.ToString());
					}
					else
					{
						textBoxHost.Clear();
						textBoxPort.Clear();
						textBoxUserName.Clear();
						textBoxPassword.Clear();
					}
				}
				else
				{
					if (!comboBoxFilePath.Text.Equals(Helper.GetResourceString(Common.Constants.COMBOBOX_DEFAULT_TEXT)))
					{
						textBoxConnection.Text = comboBoxFilePath.Text.Trim();
						toolTipForTextBox.SetToolTip(textBoxConnection, textBoxConnection.Text);
						toolTipForTextBox.SetToolTip(comboBoxFilePath, comboBoxFilePath.SelectedItem.ToString());
					}
					else
					{
						textBoxConnection.Clear();
					}
					
				}
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}

		

		#endregion

		#region comboBoxFilePath_DropdownItemSelected
		private void comboBoxFilePath_DropdownItemSelected(object sender, ToolTipComboBox.DropdownItemSelectedEventArgs e)
		{
			try
			{
				if (e.SelectedItem < 0 || e.Scrolled) toolTipForTextBox.Hide(comboBoxFilePath);
				else
					toolTipForTextBox.Show(comboBoxFilePath.Items[e.SelectedItem].ToString(), comboBoxFilePath, e.Bounds.Location.X + Cursor.Size.Width, e.Bounds.Location.Y + Cursor.Size.Height);
			}
			catch (Exception ex)
			{
				LoggingHelper.HandleException(ex);
			}
		}
		#endregion

		#region buttonCancel_Click
		private void buttonCancel_Click(object sender, EventArgs e)
		{
			try
			{
				textBoxPort.Clear();
				textBoxHost.Clear();
				textBoxConnection.Clear();
				textBoxPassword.Clear();
				textBoxUserName.Clear();

                loginToolWindow.Close(vsSaveChanges.vsSaveChangesNo);
				Helper.CheckIfLoginWindowIsVisible = false;
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}
		#endregion

		private void textBoxPort_KeyPress(object sender, KeyPressEventArgs e)
		{
			try
			{
				char c = e.KeyChar;

				//Allow only numeric charaters in filter textbox.
				if (!Helper.IsNumeric(c.ToString()))
					e.Handled = true;
			}
			catch (Exception oEx)
			{
				LoggingHelper.ShowMessage(oEx);
			}
		}

		#endregion

		private void textBoxPort_TextChanged(object sender, EventArgs e)
		{
			int result;
			if (!Int32.TryParse(textBoxPort.Text.Trim(), out result))
			{
				textBoxPort.Text = string.Empty;
			}
		}

		private void textBoxPort_KeyDown(object sender, KeyEventArgs e)
		{
			if (e.Modifiers == Keys.Control)
				e.Handled = true;
		}

		
	}
}
