namespace com.db4o
{
	/// <exclude></exclude>
	public class Config4Class : com.db4o.Config4Abstract, com.db4o.config.ObjectClass
		, com.db4o.foundation.DeepClone
	{
		private readonly com.db4o.Config4Impl _configImpl;

		private static readonly com.db4o.foundation.KeySpec CALL_CONSTRUCTOR = new com.db4o.foundation.KeySpec
			(0);

		private static readonly com.db4o.foundation.KeySpec EXCEPTIONAL_FIELDS = new com.db4o.foundation.KeySpec
			(null);

		private static readonly com.db4o.foundation.KeySpec GENERATE_UUIDS = new com.db4o.foundation.KeySpec
			(0);

		private static readonly com.db4o.foundation.KeySpec GENERATE_VERSION_NUMBERS = new 
			com.db4o.foundation.KeySpec(0);

		/// <summary>
		/// We are running into cyclic dependancies on reading the PBootRecord
		/// object, if we maintain MetaClass information there
		/// </summary>
		private static readonly com.db4o.foundation.KeySpec MAINTAIN_METACLASS = new com.db4o.foundation.KeySpec
			(true);

		private static readonly com.db4o.foundation.KeySpec MAXIMUM_ACTIVATION_DEPTH = new 
			com.db4o.foundation.KeySpec(0);

		private static readonly com.db4o.foundation.KeySpec METACLASS = new com.db4o.foundation.KeySpec
			(null);

		private static readonly com.db4o.foundation.KeySpec MINIMUM_ACTIVATION_DEPTH = new 
			com.db4o.foundation.KeySpec(0);

		private static readonly com.db4o.foundation.KeySpec PERSIST_STATIC_FIELD_VALUES = 
			new com.db4o.foundation.KeySpec(false);

		private static readonly com.db4o.foundation.KeySpec QUERY_ATTRIBUTE_PROVIDER = new 
			com.db4o.foundation.KeySpec(null);

		private static readonly com.db4o.foundation.KeySpec STORE_TRANSIENT_FIELDS = new 
			com.db4o.foundation.KeySpec(false);

		private static readonly com.db4o.foundation.KeySpec TRANSLATOR = new com.db4o.foundation.KeySpec
			(null);

		private static readonly com.db4o.foundation.KeySpec TRANSLATOR_NAME = new com.db4o.foundation.KeySpec
			((string)null);

		private static readonly com.db4o.foundation.KeySpec UPDATE_DEPTH = new com.db4o.foundation.KeySpec
			(0);

		private static readonly com.db4o.foundation.KeySpec WRITE_AS = new com.db4o.foundation.KeySpec
			((string)null);

		private bool _processing;

		protected Config4Class(com.db4o.Config4Impl configuration, com.db4o.foundation.KeySpecHashtable4
			 config) : base(config)
		{
			_configImpl = configuration;
		}

		internal Config4Class(com.db4o.Config4Impl a_configuration, string a_name)
		{
			_configImpl = a_configuration;
			SetName(a_name);
		}

		internal virtual int AdjustActivationDepth(int a_depth)
		{
			if ((CascadeOnActivate() == com.db4o.YapConst.YES) && a_depth < 2)
			{
				a_depth = 2;
			}
			if ((CascadeOnActivate() == com.db4o.YapConst.NO) && a_depth > 1)
			{
				a_depth = 1;
			}
			if (Config().ClassActivationDepthConfigurable())
			{
				int minimumActivationDepth = _config.GetAsInt(MINIMUM_ACTIVATION_DEPTH);
				if (minimumActivationDepth != 0)
				{
					if (a_depth < minimumActivationDepth)
					{
						a_depth = minimumActivationDepth;
					}
				}
				int maximumActivationDepth = _config.GetAsInt(MAXIMUM_ACTIVATION_DEPTH);
				if (maximumActivationDepth != 0)
				{
					if (a_depth > maximumActivationDepth)
					{
						a_depth = maximumActivationDepth;
					}
				}
			}
			return a_depth;
		}

		public virtual void CallConstructor(bool flag)
		{
			PutThreeValued(CALL_CONSTRUCTOR, flag);
		}

		internal override string ClassName()
		{
			return GetName();
		}

		internal virtual com.db4o.reflect.ReflectClass ClassReflector()
		{
			return Config().Reflector().ForName(GetName());
		}

		public virtual void Compare(com.db4o.config.ObjectAttribute comparator)
		{
			_config.Put(QUERY_ATTRIBUTE_PROVIDER, comparator);
		}

		internal virtual com.db4o.Config4Field ConfigField(string fieldName)
		{
			com.db4o.foundation.Hashtable4 exceptionalFields = ExceptionalFieldsOrNull();
			if (exceptionalFields == null)
			{
				return null;
			}
			return (com.db4o.Config4Field)exceptionalFields.Get(fieldName);
		}

		public virtual object DeepClone(object param)
		{
			return new com.db4o.Config4Class((com.db4o.Config4Impl)param, _config);
		}

		public virtual void EnableReplication(bool setting)
		{
			GenerateUUIDs(setting);
			GenerateVersionNumbers(setting);
		}

		public virtual void GenerateUUIDs(bool setting)
		{
			PutThreeValued(GENERATE_UUIDS, setting);
		}

		public virtual void GenerateVersionNumbers(bool setting)
		{
			PutThreeValued(GENERATE_VERSION_NUMBERS, setting);
		}

		public virtual com.db4o.config.ObjectTranslator GetTranslator()
		{
			com.db4o.config.ObjectTranslator translator = (com.db4o.config.ObjectTranslator)_config
				.Get(TRANSLATOR);
			if (translator != null)
			{
				return translator;
			}
			string translatorName = _config.GetAsString(TRANSLATOR_NAME);
			if (translatorName == null)
			{
				return null;
			}
			try
			{
				translator = (com.db4o.config.ObjectTranslator)Config().Reflector().ForName(translatorName
					).NewInstance();
			}
			catch (System.Exception t)
			{
				com.db4o.Messages.LogErr(Config(), 48, translatorName, null);
				TranslateOnDemand(null);
			}
			Translate(translator);
			return translator;
		}

		public virtual bool InitOnUp(com.db4o.Transaction systemTrans, int[] metaClassID)
		{
			if (_processing)
			{
				return false;
			}
			_processing = true;
			com.db4o.YapStream stream = systemTrans.Stream();
			if (stream.MaintainsIndices())
			{
				bool maintainMetaClass = _config.GetAsBoolean(MAINTAIN_METACLASS);
				if (maintainMetaClass)
				{
					com.db4o.MetaClass metaClassRef = MetaClass();
					if (metaClassID[0] > 0)
					{
						metaClassRef = (com.db4o.MetaClass)stream.GetByID1(systemTrans, metaClassID[0]);
						_config.Put(METACLASS, metaClassRef);
					}
					if (metaClassRef == null)
					{
						metaClassRef = (com.db4o.MetaClass)stream.Get1(systemTrans, new com.db4o.MetaClass
							(GetName())).Next();
						_config.Put(METACLASS, metaClassRef);
						metaClassID[0] = stream.GetID1(systemTrans, metaClassRef);
					}
					if (metaClassRef == null)
					{
						metaClassRef = new com.db4o.MetaClass(GetName());
						_config.Put(METACLASS, metaClassRef);
						stream.SetInternal(systemTrans, metaClassRef, int.MaxValue, false);
						metaClassID[0] = stream.GetID1(systemTrans, metaClassRef);
					}
					else
					{
						stream.Activate1(systemTrans, metaClassRef, int.MaxValue);
					}
				}
			}
			_processing = false;
			return true;
		}

		internal virtual object Instantiate(com.db4o.YapStream a_stream, object a_toTranslate
			)
		{
			return ((com.db4o.config.ObjectConstructor)_config.Get(TRANSLATOR)).OnInstantiate
				(a_stream, a_toTranslate);
		}

		internal virtual bool Instantiates()
		{
			return GetTranslator() is com.db4o.config.ObjectConstructor;
		}

		public virtual void MaximumActivationDepth(int depth)
		{
			_config.Put(MAXIMUM_ACTIVATION_DEPTH, depth);
		}

		public virtual void MinimumActivationDepth(int depth)
		{
			_config.Put(MINIMUM_ACTIVATION_DEPTH, depth);
		}

		public virtual int CallConstructor()
		{
			if (_config.Get(TRANSLATOR) != null)
			{
				return com.db4o.YapConst.YES;
			}
			return _config.GetAsInt(CALL_CONSTRUCTOR);
		}

		private com.db4o.foundation.Hashtable4 ExceptionalFieldsOrNull()
		{
			return (com.db4o.foundation.Hashtable4)_config.Get(EXCEPTIONAL_FIELDS);
		}

		private com.db4o.foundation.Hashtable4 ExceptionalFields()
		{
			com.db4o.foundation.Hashtable4 exceptionalFieldsCollection = ExceptionalFieldsOrNull
				();
			if (exceptionalFieldsCollection == null)
			{
				exceptionalFieldsCollection = new com.db4o.foundation.Hashtable4(16);
				_config.Put(EXCEPTIONAL_FIELDS, exceptionalFieldsCollection);
			}
			return exceptionalFieldsCollection;
		}

		public virtual com.db4o.config.ObjectField ObjectField(string fieldName)
		{
			com.db4o.foundation.Hashtable4 exceptionalFieldsCollection = ExceptionalFields();
			com.db4o.Config4Field c4f = (com.db4o.Config4Field)exceptionalFieldsCollection.Get
				(fieldName);
			if (c4f == null)
			{
				c4f = new com.db4o.Config4Field(this, fieldName);
				exceptionalFieldsCollection.Put(fieldName, c4f);
			}
			return c4f;
		}

		public virtual void PersistStaticFieldValues()
		{
			_config.Put(PERSIST_STATIC_FIELD_VALUES, true);
		}

		internal virtual bool QueryEvaluation(string fieldName)
		{
			com.db4o.foundation.Hashtable4 exceptionalFields = ExceptionalFieldsOrNull();
			if (exceptionalFields != null)
			{
				com.db4o.Config4Field field = (com.db4o.Config4Field)exceptionalFields.Get(fieldName
					);
				if (field != null)
				{
					return field.QueryEvaluation();
				}
			}
			return true;
		}

		public virtual void ReadAs(object clazz)
		{
			com.db4o.Config4Impl configRef = Config();
			com.db4o.reflect.ReflectClass claxx = configRef.ReflectorFor(clazz);
			if (claxx == null)
			{
				return;
			}
			_config.Put(WRITE_AS, GetName());
			configRef.ReadAs().Put(GetName(), claxx.GetName());
		}

		public virtual void Rename(string newName)
		{
			Config().Rename(new com.db4o.Rename("", GetName(), newName));
			SetName(newName);
		}

		public virtual void StoreTransientFields(bool flag)
		{
			_config.Put(STORE_TRANSIENT_FIELDS, flag);
		}

		public virtual void Translate(com.db4o.config.ObjectTranslator translator)
		{
			if (translator == null)
			{
				_config.Put(TRANSLATOR_NAME, null);
			}
			_config.Put(TRANSLATOR, translator);
		}

		internal virtual void TranslateOnDemand(string a_translatorName)
		{
			_config.Put(TRANSLATOR_NAME, a_translatorName);
		}

		public virtual void UpdateDepth(int depth)
		{
			_config.Put(UPDATE_DEPTH, depth);
		}

		internal virtual com.db4o.Config4Impl Config()
		{
			return _configImpl;
		}

		internal virtual int GenerateUUIDs()
		{
			return _config.GetAsInt(GENERATE_UUIDS);
		}

		internal virtual int GenerateVersionNumbers()
		{
			return _config.GetAsInt(GENERATE_VERSION_NUMBERS);
		}

		internal virtual void MaintainMetaClass(bool flag)
		{
			_config.Put(MAINTAIN_METACLASS, flag);
		}

		internal virtual com.db4o.MetaClass MetaClass()
		{
			return (com.db4o.MetaClass)_config.Get(METACLASS);
		}

		internal virtual bool StaticFieldValuesArePersisted()
		{
			return _config.GetAsBoolean(PERSIST_STATIC_FIELD_VALUES);
		}

		internal virtual com.db4o.config.ObjectAttribute QueryAttributeProvider()
		{
			return (com.db4o.config.ObjectAttribute)_config.Get(QUERY_ATTRIBUTE_PROVIDER);
		}

		internal virtual bool StoreTransientFields()
		{
			return _config.GetAsBoolean(STORE_TRANSIENT_FIELDS);
		}

		internal virtual int UpdateDepth()
		{
			return _config.GetAsInt(UPDATE_DEPTH);
		}

		internal virtual string WriteAs()
		{
			return _config.GetAsString(WRITE_AS);
		}
	}
}
