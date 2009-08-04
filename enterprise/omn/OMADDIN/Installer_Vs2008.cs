using System;
using System.IO;
using System.Collections;
using System.ComponentModel;
using System.Configuration.Install;
using System.Windows.Forms;
using System.Xml;

namespace OMAddin
{
	[RunInstaller(true)]
	public partial class OMInstaller : Installer
	{
		public OMInstaller()
		{
			InitializeComponent();
		}

		protected override void OnAfterUninstall(IDictionary savedState)
		{
			base.OnAfterUninstall(savedState);
			
			DeleteApplicationDataFolder();
			string yearVersion = Context.Parameters["version"];
			RemoveVSVersionFromAddinFile(yearVersion, TargetAddinFilePath());
			
			FixNonLocalizedVSConfig(delegate(string nonLocalizedAddinPath)
									{
										RemoveVSVersionFromAddinFile(yearVersion, nonLocalizedAddinPath);
									});
		}

		protected override void OnAfterInstall(IDictionary savedState)
		{
			try
			{
				base.OnAfterInstall(savedState);
				DeleteApplicationDataFolder();

				string yearVersion = Context.Parameters["version"];
				string addinPath = Context.Parameters["assemblypath"];

				UpdateAddinFile(yearVersion, addinPath, TargetAddinFilePath());

				FixNonLocalizedVSConfig(delegate(string nonLocalizedAddinPath)
				                        {
											UpdateAddinFile(yearVersion, addinPath, nonLocalizedAddinPath);
										});

				CopyWindowsPRFFile(InstallFolderFrom(addinPath), yearVersion);
				InvokeReadMe(Path.GetDirectoryName(addinPath));
			}
			catch (Exception ex)
			{
				MessageBox.Show(ex.ToString());
			}
		}

		private static void FixNonLocalizedVSConfig(Action<string> action)
		{
			string localizedAddinPath = TargetAddinFilePath();
			string nonLocalizedAddinPath = NonLocalized(localizedAddinPath);

			if (localizedAddinPath != nonLocalizedAddinPath)
			{
				action(nonLocalizedAddinPath);
			}
		}

		private static string NonLocalized(string localizedAddinPath)
		{
			return localizedAddinPath.Replace(LastFolderName(VSGlobalAddinConfigurationFolder()), "Application Data");
		}

		private static string LastFolderName(string localizedConfigFolder)
		{
			int backSlashIndex = localizedConfigFolder.LastIndexOf(Path.DirectorySeparatorChar);
			return backSlashIndex >= 0 ? localizedConfigFolder.Substring(backSlashIndex + 1) : localizedConfigFolder;
		}

		private static void RemoveVSVersionFromAddinFile(string yearVersion, string addinFilePath)
		{
			XmlDocument addinDoc = OpenAddinFile(addinFilePath, addinFilePath);
			if (addinDoc != null)
			{
				XmlNode toBeRemoved = addinDoc.SelectSingleNode(HostApplicationPathForVersion(VSVersionNumberFor(yearVersion)), NameSpaceManagerFor(addinDoc, ""));
				toBeRemoved.ParentNode.RemoveChild(toBeRemoved);

				SaveAddinFile(addinDoc, addinFilePath);
			}
		}

		private static string InstallFolderFrom(string path)
		{
			return Path.GetDirectoryName(path);
		}

		private static void UpdateAddinFile(string yearVersion, string addinInstallationSourcePath, string addinFilePath)
		{
			XmlDocument addinDoc = OpenAddinFile(addinInstallationSourcePath, addinFilePath);

			UpdateNode(addinDoc, "/ns:Extensibility/ns:Addin/ns:Assembly", addinInstallationSourcePath);

			AddVisualStudioVersion(addinDoc, VSVersionNumberFor(yearVersion));

			SaveAddinFile(addinDoc, addinFilePath);
		}

		private static XmlDocument OpenAddinFile(string addinAssemblyPath, string addinFilePath)
		{
			string addinFileToLoad = File.Exists(addinFilePath) ? addinFilePath : InstalledAddinFilePath(addinAssemblyPath);
			if (!File.Exists(addinFileToLoad))
				return null;

			XmlDocument addinDoc = new XmlDocument();

			addinDoc.Load(addinFileToLoad);

			return addinDoc;
		}

		private static void SaveAddinFile(XmlDocument addinDoc, string addinFilePath)
		{
			Directory.CreateDirectory(Path.GetDirectoryName(addinFilePath));
			addinDoc.Save(addinFilePath);
		}

		private static string InstalledAddinFilePath(string path)
		{
			return Path.Combine(InstallFolderFrom(path), "OMAddin.addin");
		}

		private static XmlNamespaceManager NameSpaceManagerFor(XmlDocument addinDoc, string prefix)
		{
			XmlNamespaceManager nsmgr = new XmlNamespaceManager(addinDoc.NameTable);
			nsmgr.AddNamespace("ns", addinDoc.DocumentElement.GetNamespaceOfPrefix(prefix));

			return nsmgr;
		}

		private static void AddVisualStudioVersion(XmlDocument addinDoc, string version)
		{
			XmlNamespaceManager nsmgr = NameSpaceManagerFor(addinDoc, "");
			XmlNode node = addinDoc.SelectSingleNode(HostApplicationPathForVersion(version), nsmgr);
			if (node == null)
			{
				XmlNode addinNode = addinDoc.SelectSingleNode("/ns:Extensibility", nsmgr);

				XmlNode hostApplicationNode = AddElementTo(addinNode, "HostApplication");

				AddElementTo(hostApplicationNode, "Name", "Microsoft Visual Studio");
				AddElementTo(hostApplicationNode, "Version", version);
			}
		}

		private static string HostApplicationPathForVersion(string version)
		{
			return String.Format("/ns:Extensibility/ns:HostApplication[ns:Version[text()={0}]]", version);
		}

		private static XmlNode AddElementTo(XmlNode targetNode, string nodeName)
		{
			XmlElement node = targetNode.OwnerDocument.CreateElement(nodeName, targetNode.NamespaceURI);
			targetNode.AppendChild(node);

			return node;
		}

		private static void AddElementTo(XmlNode targetNode, string nodeName, string nodeValue)
		{
			XmlNode node = AddElementTo(targetNode, nodeName);
			node.InnerText = nodeValue;
		}

		private static void UpdateNode(XmlDocument addinDoc, string nodePath, string value)
		{
			XmlNode node = addinDoc.SelectSingleNode(nodePath, NameSpaceManagerFor(addinDoc, ""));
			node.FirstChild.Value = value;
		}

		internal static void CopyWindowsPRFFile(string installFolder, string yearVersion)
		{
			string targetFile = Path.Combine(VSProfilePathFor(yearVersion), "windows.prf");
			string sourceFile = Path.Combine(installFolder, "windows.prf");
			if (File.Exists(sourceFile))
			{
				File.Copy(sourceFile, targetFile, true);
				File.Delete(sourceFile);
			}
		}

		internal static void InvokeReadMe(string basePath)
		{
			String readmeFilePath = Path.Combine(basePath, @"ReadMe\ReadMe.htm");

			if (File.Exists(readmeFilePath))
			{
				System.Diagnostics.Process.Start(readmeFilePath);
			}
		}

		private static void DeleteApplicationDataFolder()
		{
			string path = Folder.DB4OHome;
			Folder.Delete(path);
			if (!Directory.Exists(Folder.OMNHome))
			{
				Directory.CreateDirectory(Folder.OMNHome);
			}
		}

		private static string VSGlobalAddinConfigurationFolder()
		{
			return Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData);
		}

		private static string VSProfilePathFor(string version)
		{
			return Path.Combine(
						Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
						string.Format(@"Microsoft\VisualStudio\{0}", VSVersionNumberFor(version)));
		}

		private static string VSVersionNumberFor(string yearVersion)
		{
			if (yearVersion == "2005") return "8.0";
			if (yearVersion == "2008") return "9.0";

			throw new ArgumentException("Unsuported Visual Studio version: " + yearVersion, "yearVersion");
		}

		private static string TargetAddinFilePath()
		{
			return Path.Combine(VSGlobalAddinConfigurationFolder(), @"Microsoft\MSEnvShared\Addins\OMAddin.addin");
		}
	}
}