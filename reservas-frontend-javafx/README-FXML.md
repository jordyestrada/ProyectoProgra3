# 🎯 Integración FXML + SceneBuilder - Guía de Prueba

## ✅ Lo que se agregó

### Estructura nueva:
```
reservas-frontend-javafx/
  src/main/
    ├── java/cr/una/reservas/frontend/ui/test/
    │   ├── TestController.java          ← Controller FXML
    │   └── TestViewLoader.java          ← Loader helper
    └── resources/fxml/
        └── test.fxml                    ← Vista diseñable en SceneBuilder
```

### Modificaciones:
- ✅ `LoginView.java` → Agregado botón "🔧 Probar FXML + SceneBuilder"
- ✅ Todas las demás clases **intactas** (no se modificó código existente)

---

## 🚀 Cómo probar

### 1. Ejecutar la aplicación:
```bash
cd reservas-frontend-javafx
.\mvnw.cmd javafx:run
```

### 2. En la ventana de Login:
- Verás un nuevo botón azul: **"🔧 Probar FXML + SceneBuilder"**
- Haz clic en ese botón
- Se abrirá la vista de prueba cargada desde FXML

### 3. En la vista de prueba:
- Escribe algo en el campo de texto
- Haz clic en "Probar" → Verás el resultado
- Haz clic en "Limpiar" → Limpia el campo
- Haz clic en "← Volver al Login" → Regresa al login

---

## 🎨 Usar SceneBuilder

### Instalar SceneBuilder:
1. Descargar desde: https://gluonhq.com/products/scene-builder/
2. Instalar el ejecutable

### Configurar en VS Code:
1. Instalar extensión: **"Extension Pack for Java"**
2. Settings (`Ctrl+,`) → Buscar "scenebuilder"
3. Configurar: `javafx.scenebuilder.home` → Ruta de instalación

### Editar el FXML:
1. Abrir: `src/main/resources/fxml/test.fxml`
2. Click derecho → **"Open in SceneBuilder"**
3. Arrastrar componentes, modificar propiedades
4. Guardar → El archivo XML se actualiza automáticamente

### Puntos clave del FXML:
- **fx:id** → Debe coincidir con `@FXML` en el Controller
- **onAction** → Ejemplo: `#handleTest` llama a `@FXML private void handleTest()`
- **fx:controller** → Ruta completa de la clase Controller

---

## 📋 Cómo funciona la integración

### Flujo:
```
test.fxml (diseño visual)
    ↓
TestViewLoader.load() carga el FXML
    ↓
FXMLLoader instancia TestController
    ↓
@FXML campos se inyectan automáticamente
    ↓
initialize() se ejecuta
    ↓
Usuario interactúa → @FXML métodos se ejecutan
```

### Ejemplo de binding FXML ↔ Java:

**En test.fxml:**
```xml
<TextField fx:id="inputField" promptText="Ingresa un texto..."/>
<Button fx:id="testButton" text="Probar" onAction="#handleTest"/>
```

**En TestController.java:**
```java
@FXML private TextField inputField;  // ← Mismo nombre que fx:id
@FXML private Button testButton;

@FXML
private void handleTest() {          // ← Mismo nombre que onAction
    String texto = inputField.getText();
    // ...
}
```

---

## 🔄 Próximos pasos (si quieres continuar con FXML)

### Opción A: Crear más vistas de prueba
- Duplicar `test.fxml` → `spaces.fxml`, `reservations.fxml`
- Crear controllers correspondientes
- Diseñar en SceneBuilder

### Opción B: Mantener código actual
- Dejar vistas actuales como están (código Java)
- Usar FXML solo para vistas nuevas complejas

### Opción C: Migración completa
- Convertir todas las vistas existentes a FXML
- Más trabajo pero más diseñable visualmente

---

## ✅ Verificación de integración exitosa

Si ves estos mensajes en consola cuando haces clic en el botón de prueba:
```
✅ FXML cargado correctamente
✅ TestController inicializado correctamente
✅ Vista de prueba FXML cargada exitosamente
```

**¡La integración funcionó perfectamente!** 🎉

---

## 📝 Notas

- **Todo el código original está intacto** (solo se agregó un botón en LoginView)
- **SceneBuilder es opcional** (puedes editar el XML manualmente si prefieres)
- **Puedes mezclar** código Java puro con FXML en el mismo proyecto
- **La compilación sigue funcionando** igual que antes

---

## 🆘 Solución de problemas

### Error: "FXML no carga"
- Verificar que la ruta es `/fxml/test.fxml` (con barra inicial)
- Verificar que `fx:controller` tiene la ruta completa correcta

### Error: "@FXML campos son null"
- Verificar que `fx:id` en FXML coincide con nombre de campo Java
- Verificar que el campo tiene anotación `@FXML`

### SceneBuilder no abre
- Verificar instalación de SceneBuilder
- Configurar ruta en VS Code settings

---

**¡Listo!** Ya tienes FXML + SceneBuilder integrado y funcionando. 🚀
