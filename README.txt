# Libunand - Sistema Económico para Minecraft

![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5-green.svg)
![Forge](https://img.shields.io/badge/Forge-36.2.34-orange.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)

Sistema económico completo con moneda personalizada JoJoCoin y tiendas jugador-a-jugador para Minecraft 1.16.5.

## Características

### 💰 Sistema de Moneda JoJoCoin
- Moneda personalizada que reemplaza las esmeraldas en el juego
- Balance virtual con persistencia de datos
- Comandos completos de gestión económica
- Transferencias entre jugadores

### 🏪 Sistema de Tiendas con Carteles
- Tiendas de venta [SELL]: Los jugadores venden sus items
- Tiendas de compra [BUY]: Los jugadores compran items de otros
- Cofres vinculados con partículas identificadoras
- Protección automática contra griefing

### 🎮 Integración con Vanilla
- JoJoCoins reemplazan esmeraldas en trades de aldeanos
- JoJoCoins aparecen en cofres de estructuras
- Compatible con todos los mods mediante sistema oficial de Forge

## Instalación

1. Descarga [Minecraft Forge 1.16.5-36.2.34](https://files.minecraftforge.net/)
2. Descarga `libunand-1.0.0.jar` de [Releases](https://github.com/ABFann/Libunand-Mod/releases)
3. Coloca el archivo en la carpeta `mods/` de tu instalación de Minecraft
4. ¡Inicia el juego!

## Comandos

### Economía
```
/jojocoins balance - Ver tu saldo actual
/jojocoins pay <jugador> <cantidad> - Transferir dinero
/jojocoins give <jugador> <cantidad> - Dar dinero (OP only)
/jojocoins withdraw <cantidad> - Retirar a inventario
/jojocoins deposit <cantidad> - Depositar del inventario
/jojocoins help - Mostrar ayuda
```

### Tiendas
```
/shop itemname - Ver ID del item en mano
/shop help - Ayuda del sistema de tiendas
```

## Cómo Usar las Tiendas

### Vincular Cofre
1. Coloca un cofre
2. Coloca un cartel encima
3. Escribe en el cartel:
   ```
   [SHOP]
   ```
4. El cofre quedará vinculado (verás partículas verdes)

### Crear Tienda de Venta
1. Coloca un cartel
2. Escribe:
   ```
   [SELL]
   diamond
   1
   10
   ```
   - Línea 1: `[SELL]`
   - Línea 2: Nombre del item (usa `/shop itemname`)
   - Línea 3: Cantidad
   - Línea 4: Precio en JoJoCoins

### Crear Tienda de Compra
1. Coloca un cartel
2. Escribe:
   ```
   [BUY]
   iron_ingot
   32
   50
   ```
   - Mismo formato que [SELL]

### Comprar/Vender
- Click derecho en el cartel de una tienda
- La transacción se realiza automáticamente

## Configuración

El archivo de configuración se encuentra en:
`config/libunand-common.toml`

```toml
[Economy]
    startingBalance = 100
    maxBalance = 1000000

[Plots]
    plotMergeDistance = 10
```

## Compatibilidad

- **Minecraft:** 1.16.5
- **Forge:** 36.2.34+
- **Compatible con:** WorldEdit, WorldProtection
- **Servidor:** Sí (recomendado para multijugador)
- **Cliente:** Sí

## Desarrollo

### Requisitos
- Java 8+
- IntelliJ IDEA (recomendado)
- Minecraft Forge MDK 1.16.5

### Compilar
```bash
./gradlew build
```

El `.jar` compilado estará en `build/libs/`

## Créditos

**Desarrollador:** ABFann

## Licencia

All Rights Reserved © 2025 ABFann

## Soporte

¿Problemas o sugerencias? Abre un [Issue](https://github.com/ABFann/Libunand-Mod/issues)

---

**¡Disfruta del sistema económico más completo para Minecraft!**
```
