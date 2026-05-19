package com.example.demo.service;

import com.example.demo.dto.DetallePedidoDTO;
import com.example.demo.dto.PedidoResponseDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacturaService {

    public byte[] generarFacturaPdf(PedidoResponseDTO pedido) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);

            // Título de la tienda
            Paragraph tituloTienda = new Paragraph("SHINE BOUTIQUE", titleFont);
            tituloTienda.setAlignment(Element.ALIGN_CENTER);
            tituloTienda.setSpacingAfter(20);
            document.add(tituloTienda);

            // Título Factura
            Paragraph tituloFactura = new Paragraph("FACTURA COMERCIAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            tituloFactura.setAlignment(Element.ALIGN_CENTER);
            tituloFactura.setSpacingAfter(30);
            document.add(tituloFactura);

            // Datos del pedido
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaStr = pedido.getFecha() != null ? pedido.getFecha().format(dtf) : java.time.LocalDateTime.now().format(dtf);
            
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(30);
            
            PdfPCell cellIzq = new PdfPCell();
            cellIzq.setBorder(PdfPCell.NO_BORDER);
            cellIzq.addElement(new Paragraph("Pedido ID: SHINE-" + pedido.getIdPedido(), normalFont));
            cellIzq.addElement(new Paragraph("Estado: " + pedido.getEstado().toUpperCase(), normalFont));
            
            PdfPCell cellDer = new PdfPCell();
            cellDer.setBorder(PdfPCell.NO_BORDER);
            cellDer.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pFecha = new Paragraph("Fecha: " + fechaStr, normalFont);
            pFecha.setAlignment(Element.ALIGN_RIGHT);
            cellDer.addElement(pFecha);

            infoTable.addCell(cellIzq);
            infoTable.addCell(cellDer);
            document.add(infoTable);

            // Tabla de artículos
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.5f, 2f, 2f});
            table.setSpacingBefore(10);

            // Cabeceras
            String[] cabeceras = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
            for (String cabecera : cabeceras) {
                PdfPCell hCell = new PdfPCell(new Phrase(cabecera, headerFont));
                hCell.setBackgroundColor(new Color(230, 230, 230));
                hCell.setPadding(8);
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(hCell);
            }

            // Filas
            for (DetallePedidoDTO detalle : pedido.getDetalles()) {
                PdfPCell cellNombre = new PdfPCell(new Phrase(detalle.getNombre(), normalFont));
                cellNombre.setPadding(8);
                
                PdfPCell cellCant = new PdfPCell(new Phrase(String.valueOf(detalle.getCantidad()), normalFont));
                cellCant.setPadding(8);
                cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                
                PdfPCell cellPrecio = new PdfPCell(new Phrase("€" + detalle.getPrecioUnitario().toPlainString(), normalFont));
                cellPrecio.setPadding(8);
                cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                
                PdfPCell cellSub = new PdfPCell(new Phrase("€" + detalle.getSubtotal().toPlainString(), normalFont));
                cellSub.setPadding(8);
                cellSub.setHorizontalAlignment(Element.ALIGN_RIGHT);

                table.addCell(cellNombre);
                table.addCell(cellCant);
                table.addCell(cellPrecio);
                table.addCell(cellSub);
            }
            document.add(table);

            // Total
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{7.5f, 2f});
            totalTable.setSpacingBefore(20);
            
            PdfPCell cEmpty = new PdfPCell(new Phrase(""));
            cEmpty.setBorder(PdfPCell.NO_BORDER);
            totalTable.addCell(cEmpty);
            
            PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL: €" + pedido.getTotal().toPlainString(), totalFont));
            cTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotal.setBorder(PdfPCell.NO_BORDER);
            totalTable.addCell(cTotal);
            
            document.add(totalTable);
            
            // Pie de página
            Paragraph footer = new Paragraph("Gracias por tu compra en Shine Boutique.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(50);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
