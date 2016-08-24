
package de.pleumann.antenna;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import de.pleumann.antenna.misc.Base64;
import de.pleumann.antenna.misc.JadFile;

/**
 * @author omry Created on Nov 29, 2004
 */
public class WtkSign extends Task
{
	/**
	 * Key store path. optional, defaults to $HOME/.keystore
	 */
	private File m_keyStore;

	/**
	 * The jar file to sign. required.
	 */
	private File m_jarFile;

	/**
	 * The jad file to put the signature into. required.
	 */
	private File m_jadFile;

	/**
	 * The password for the keystore. defaults to empty string.
	 */
	private String m_storePass;

	/**
	 * The password for the certificate we are using. defaults to empty string.
	 */
	private String m_certPass;

	/**
	 * The alias for the certificate. required.
	 */
	private String m_certAlias;

	/**
	 * Cert number is used when signing with multiple certificates. Optional,
	 * defaults to 1.
	 */
	private int m_certNum;

	/**
	 * The encoding of the jad file. defualts to UTF-8
	 */
	private String m_jadEncoding;

	public WtkSign()
	{
		m_keyStore = new File(System.getProperty("user.home") + File.separator
				+ ".keystore");
		m_certNum = 1;
		m_certPass = "";
		m_storePass = "";
		m_jadEncoding = "UTF-8";
	}

	/**
	 * @param certAlias The certAlias to set.
	 */
	public void setCertAlias(String certAlias)
	{
		m_certAlias = certAlias;
	}

	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException
	{
		if(m_jarFile == null)
		{
			throw new BuildException("jarfile is not set");
		}

		if(m_jadFile == null)
		{
			throw new BuildException("jadfile is not set");
		}

		if(m_certAlias == null)
		{
			throw new BuildException("certalias is not set");
		}

		if(!(m_jarFile.exists()))
		{
			throw new BuildException("jarfile " + m_jarFile + " does not exist");
		}

		if(!(m_jadFile.exists()))
		{
			throw new BuildException("jadfile " + m_jadFile + " does not exist");
		}

		FileInputStream ksin = null; // keystore input stream
		FileInputStream jin = null; // jar input stream
		try
		{
			JadFile jadfile = new JadFile();
			jadfile.load(m_jadFile.getAbsolutePath(), m_jadEncoding);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			ksin = new FileInputStream(m_keyStore);
			keystore.load(ksin, m_storePass.toCharArray());
			// add certificates chain.
			Certificate[] certificates = keystore.getCertificateChain(m_certAlias);
			if(certificates == null)
			{
				throw new BuildException("Certificate chain " + m_certAlias + " not found in key store");
			}
			else
			{
				for (int i = 0; i < certificates.length; i++)
				{
					String key = "MIDlet-Certificate-" + m_certNum + "-"
							+ (i + 1);
					String value = Base64.encodeBytes(certificates[i].getEncoded(), Base64.DONT_BREAK_LINES);
					jadfile.setValue(key, value);
				}
			}
			
			// sign the jar
			jin = new FileInputStream(m_jarFile);
			
			log("Signing jar " + m_jarFile);
			log("Key store : " + m_keyStore);
			log("Cert alias : " + m_certAlias);
			
			Signature signature = Signature.getInstance("SHA1withRSA");
			
			PrivateKey key = (PrivateKey) keystore.getKey(m_certAlias, m_certPass.toCharArray());
			signature.initSign(key);
			int len;
			int t = 0;
			byte buf[] = new byte[4096];
			while ((len = jin.read(buf)) != -1)
			{
				t += len;
				signature.update(buf, 0, len);
			}
			
			byte[] sign = signature.sign();
			String sigStr = Base64.encodeBytes(sign, Base64.DONT_BREAK_LINES);
			jadfile.setValue("MIDlet-Jar-RSA-SHA1", sigStr);
			jadfile.save(m_jadFile.getAbsolutePath(), m_jadEncoding);
		}
		catch (IOException e)
		{
			error(e);
		}
		catch (KeyStoreException e)
		{
			error(e);
		}
		catch (NoSuchAlgorithmException e)
		{
			error(e);
		}
		catch (CertificateException e)
		{
			error(e);
		}
		catch (UnrecoverableKeyException e)
		{
			error(e);
		}
		catch (InvalidKeyException e)
		{
			error(e);
		}
		catch (SignatureException e)
		{
			error(e);
		}
		finally
		{
			if(ksin != null)
			{
				try
				{
					ksin.close();
				}
				catch (IOException e1)
				{
				}
			}
			if(jin != null)
			{
				try
				{
					jin.close();
				}
				catch (IOException e1)
				{
				}
			}
			
		}
	}

	/**
	 * @param e
	 */
	private void error(Exception e)
	{
		log(e.getMessage());
		throw new BuildException(e);
	}

	/**
	 * @param certNum The certNum to set.
	 */
	public void setCertNum(int certNum)
	{
		m_certNum = certNum;
	}

	/**
	 * @param jadFile The jadFile to set.
	 */
	public void setJadFile(File jadFile)
	{
		m_jadFile = jadFile;
	}

	/**
	 * @param jarFile The jarFile to set.
	 */
	public void setJarFile(File jarFile)
	{
		m_jarFile = jarFile;
	}

	/**
	 * @param keypass The keypass to set.
	 */
	public void setCertPass(String keypass)
	{
		m_certPass = keypass;
	}

	/**
	 * @param keyStore The keyStore to set.
	 */
	public void setKeyStore(File keyStore)
	{
		m_keyStore = keyStore;
	}

	/**
	 * @param storePass The storePass to set.
	 */
	public void setStorePass(String storePass)
	{
		m_storePass = storePass;
	}

	/**
	 * @param jadEncoding The jadEncoding to set.
	 */
	public void setJadEncoding(String jadEncoding)
	{
		m_jadEncoding = jadEncoding;
	}
}
